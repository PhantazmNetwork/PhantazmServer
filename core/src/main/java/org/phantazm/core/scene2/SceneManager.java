package org.phantazm.core.scene2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.Acquired;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.thread.ThreadProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewImpl;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

/**
 * SceneManager is used to fulfill requests by one or more players to join {@link Scene} objects. This class also
 * manages the lifecycle of every Scene object; creation, ticking, and eventually removal.
 * <p>
 * <h2>Thread Safety</h2>
 * Unless otherwise indicated, all methods are completely thread-safe.
 */
public final class SceneManager implements Tickable {
    /**
     * Represents the result of an attempted join for one or more players.
     */
    public enum JoinResult {
        /**
         * The join operation completed successfully. The scene was able to accept all necessary players.
         */
        JOINED,

        /**
         * No scene with the same type as was requested in the join exists. No players have been moved or had any state
         * changed.
         */
        UNRECOGNIZED_TYPE,

        /**
         * One or more players are already trying to join a scene. No players have been moved or had any state changed.
         */
        ALREADY_JOINING,

        /**
         * No existing scenes were found, and no new scenes could be created in order to fulfill this request. No
         * players have been moved or had any state changed.
         */
        CANNOT_PROVISION
    }

    private final Executor executor;
    private final Map<Class<? extends Scene>, Set<Scene>> mappedScenes;
    private final PlayerViewProvider viewProvider;
    private final ThreadDispatcher<Scene> threadDispatcher;

    public SceneManager(@NotNull Executor executor, @NotNull Set<Class<? extends Scene>> sceneTypes,
        @NotNull PlayerViewProvider viewProvider, int numThreads) {
        this.executor = Objects.requireNonNull(executor);
        this.mappedScenes = buildSceneMap(Set.copyOf(sceneTypes));
        this.viewProvider = Objects.requireNonNull(viewProvider);
        this.threadDispatcher = ThreadDispatcher.of(ThreadProvider.counter(), numThreads);

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, this::onDisconnect);
    }

    public SceneManager(@NotNull Executor executor, @NotNull Set<Class<? extends Scene>> sceneTypes,
        @NotNull PlayerViewProvider viewProvider) {
        this(executor, sceneTypes, viewProvider, Runtime.getRuntime().availableProcessors());
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends Scene>, Set<Scene>> buildSceneMap(Collection<Class<? extends Scene>> sceneTypes) {
        Map.Entry<Class<? extends Scene>, Set<Scene>>[] entries = new Map.Entry[sceneTypes.size()];
        int i = 0;
        for (Class<? extends Scene> sceneType : sceneTypes) {
            entries[i++] = Map.entry(sceneType, new CopyOnWriteArraySet<>());
        }

        return Map.ofEntries(entries);
    }

    private void onDisconnect(@NotNull PlayerDisconnectEvent disconnectEvent) {
        Player player = disconnectEvent.getPlayer();
        PlayerViewImpl view = (PlayerViewImpl) viewProvider.fromPlayer(player);

        Semaphore semaphore = view.joinSemaphore();
        semaphore.acquireUninterruptibly();
        try {
            view.currentScene().ifPresent(scene -> scene.getAcquirable().sync(self -> {
                    self.leave(Set.of(view));
                })
            );

            view.updateCurrentScene(null);
        } finally {
            semaphore.release();
        }
    }

    /**
     * Removes a scene, shutting it down and scheduling it for removal from internal data structures. If the scene is
     * not managed by this manager, or is of an unrecognized type, this method will do nothing.
     * <p>
     * Note that this method will remove any players in the scene when it has shut down, but these players will not be
     * sent anywhere.
     *
     * @param scene the scene to remove
     */
    public void removeScene(@NotNull Scene scene) {
        Set<Scene> scenes = mappedScenes.get(scene.getClass());
        if (scenes == null) {
            return;
        }

        scene.getAcquirable().async(self -> {
            if (self.isShutdown()) {
                return;
            }

            if (!scenes.remove(self)) {
                return;
            }

            threadDispatcher.removeElement(self);
            threadDispatcher.deletePartition(self);

            self.shutdown();

            Set<PlayerView> players = self.players();
            try {
                lockPlayers(players, true);
                self.leave(players);

                for (PlayerView playerView : players) {
                    ((PlayerViewImpl) playerView).updateCurrentScene(null);
                }
            } finally {
                unlockPlayers(players);
            }
        });
    }

    /**
     * Retrieves all scenes of a specified type. The resulting set is unmodifiable and will not change even if new
     * scenes are created after the invocation of this method.
     *
     * @param sceneType the scene type to look up
     * @param <T>       the type of scene to search for
     * @return a set of scenes of the specified type; or {@code null} if such a type has not been registered; the set
     * may additionally be empty, in which case scenes of that type <i>may</i> exist but none currently do
     */
    @SuppressWarnings("unchecked")
    public <T extends Scene> @Nullable @Unmodifiable Set<T> typed(@NotNull Class<T> sceneType) {
        return (Set<T>) Set.copyOf(mappedScenes.get(sceneType));
    }

    /**
     * Asynchronously attempts to fulfill the given {@link Join}.
     * <p>
     * Calling this method may or may not result in the creation of a new scene (a relatively expensive operation);
     * however, a best-effort attempt is always made to find a suitable existing scene.
     * <p>
     * Finally, if one or more players attempt to participate in more than 1 Join at the same time, subsequent attempts
     * will immediately fail (they will return a CompletableFuture containing {@code false}).
     *
     * @param join the Join to fulfill
     * @return a {@link CompletableFuture} containing a {@link JoinResult} indicating the success or failure of the join
     */
    public @NotNull CompletableFuture<JoinResult> joinScene(@NotNull Join<?> join) {
        Set<Scene> scenes = mappedScenes.get(join.targetType());
        if (scenes == null) {
            return CompletableFuture.completedFuture(JoinResult.UNRECOGNIZED_TYPE);
        }

        Set<PlayerView> players = join.players();
        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(JoinResult.JOINED);
        }

        if (!lockPlayers(players, false)) {
            return CompletableFuture.completedFuture(JoinResult.ALREADY_JOINING);
        }

        return CompletableFuture.supplyAsync(() -> {
            for (Scene scene : scenes) {
                if (tryJoinExisting(scene, join)) {
                    return JoinResult.JOINED;
                }
            }

            boolean canCreateNewScene = join.canCreateNewScene();
            if (!canCreateNewScene) {
                return JoinResult.CANNOT_PROVISION;
            }

            Scene newScene = createAndJoinNewScene(join);

            scenes.add(newScene);
            threadDispatcher.createPartition(newScene);
            threadDispatcher.updateElement(newScene, newScene);

            return JoinResult.JOINED;
        }, executor).whenComplete((ignored, ignored1) -> {
            unlockPlayers(join.players());
        });
    }

    private boolean lockPlayers(Collection<PlayerView> players, boolean force) {
        if (players.isEmpty()) {
            return true;
        }

        List<Semaphore> semaphores = force ? null : new ArrayList<>(players.size());
        for (PlayerView playerView : players) {
            Semaphore semaphore = ((PlayerViewImpl) playerView).joinSemaphore();
            if (force) {
                semaphore.acquireUninterruptibly();
                continue;
            }

            if (!semaphore.tryAcquire()) {
                for (Semaphore acquired : semaphores) {
                    acquired.release();
                }

                return false;
            }

            semaphores.add(semaphore);
        }

        return true;
    }

    private void unlockPlayers(Collection<PlayerView> players) {
        for (PlayerView playerView : players) {
            ((PlayerViewImpl) playerView).joinSemaphore().release();
        }
    }

    /**
     * Gets an Optional that may contain the current scene the player is in, or {@code null} if the player does not
     * belong to an existing scene.
     *
     * @param playerView the player to check
     * @return an Optional containing the current scene
     */
    public @NotNull Optional<Scene> getCurrentScene(@NotNull PlayerView playerView) {
        PlayerViewImpl view = (PlayerViewImpl) playerView;
        return view.currentScene();
    }

    /**
     * Synchronizes the given player with their current scene, such that they are guaranteed to remain a part of that
     * scene until control flow exits {@code consumer}. If the player does not have a current scene, the consumer will
     * not be called.
     * <p>
     * Note that this method will <i>not</i> acquire the current scene!
     *
     * @param playerView the player to synchronize on
     * @param consumer   the consumer to run with both the player and current scene passed as arguments
     */
    public void synchronizeWithCurrentScene(@NotNull PlayerView playerView,
        @NotNull BiConsumer<? super @NotNull PlayerView, ? super @NotNull Scene> consumer) {
        PlayerViewImpl view = (PlayerViewImpl) playerView;

        Semaphore semaphore = view.joinSemaphore();
        semaphore.acquireUninterruptibly();
        try {
            Optional<Scene> scene = view.currentScene();
            if (scene.isEmpty()) {
                return;
            }

            consumer.accept(view, scene.get());
        } finally {
            semaphore.release();
        }
    }

    private <T extends Scene> T createAndJoinNewScene(Join<T> join) {
        T scene = join.createNewScene();
        leaveOldScenes(join, scene);

        //not necessary to acquire, scene was just created but is not ticking yet
        join.join(scene);

        return scene;
    }

    private <T extends Scene> boolean tryJoinExisting(Scene scene, Join<T> join) {
        Acquirable<? extends Scene> sceneAcquirable = scene.getAcquirable();
        T castScene = join.targetType().cast(scene);

        Acquired<? extends Scene> acquired = sceneAcquirable.lock();
        try {
            if (!castScene.joinable() || !join.matches(castScene)) {
                return false;
            }

            leaveOldScenes(join, castScene);
            join.join(castScene);
        } finally {
            acquired.unlock();
        }

        return true;
    }

    private void leaveOldScenes(Join<?> join, Scene newScene) {
        Set<PlayerView> players = join.players();
        if (players.isEmpty()) {
            return;
        }

        if (players.size() == 1) {
            PlayerViewImpl onlyPlayer = (PlayerViewImpl) players.iterator().next();

            onlyPlayer.currentScene().ifPresent(scene -> {
                if (scene == newScene) {
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    self.leave(players);
                });
            });

            onlyPlayer.updateCurrentScene(newScene);
            return;
        }

        Map<Scene, Set<PlayerViewImpl>> groupedScenes = new IdentityHashMap<>(4);
        for (PlayerView playerView : players) {
            PlayerViewImpl view = (PlayerViewImpl) playerView;

            Optional<Scene> currentSceneOptional = view.currentScene();
            if (currentSceneOptional.isEmpty()) {
                view.updateCurrentScene(newScene);
                continue;
            }

            Scene currentScene = currentSceneOptional.get();
            if (currentScene == newScene) {
                continue;
            }

            groupedScenes.computeIfAbsent(currentScene, ignored -> new HashSet<>(2)).add(view);
        }

        for (Map.Entry<Scene, Set<PlayerViewImpl>> entry : groupedScenes.entrySet()) {
            Set<PlayerViewImpl> value = entry.getValue();
            entry.getKey().getAcquirable().sync(self -> {
                self.leave(value);
            });

            for (PlayerViewImpl view : value) {
                view.updateCurrentScene(newScene);
            }
        }
    }

    /**
     * Ticks this SceneManager. Should only be called by the Minestom tick scheduler at a rate of approximately 20 times
     * per second.
     *
     * @param time the time of the tick in milliseconds
     */
    @Override
    @ApiStatus.Internal
    public void tick(long time) {
        threadDispatcher.updateAndAwait(time);
        threadDispatcher.refreshThreads(System.currentTimeMillis() - time);
    }
}
