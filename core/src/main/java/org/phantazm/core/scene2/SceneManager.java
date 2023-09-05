package org.phantazm.core.scene2;

import net.minestom.server.Tickable;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * SceneManager is used to fulfill requests by one or more players to join {@link Scene} objects. This class also
 * manages the lifecycle of every Scene object; creation, ticking, and eventually removal. Several different utility
 * methods, such as {@link SceneManager#getCurrentScene(PlayerView)}, are also provided to make working with scenes
 * easier.
 * <p>
 * This class is intended to be used as a singleton; generally, most applications will only want to have a one
 * {@link SceneManager}.
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

    private record SceneEntry(Set<Scene> scenes,
        Object creationLock) {
    }

    private final Executor executor;
    private final Map<Class<? extends Scene>, SceneEntry> mappedScenes;
    private final ThreadDispatcher<Scene> threadDispatcher;

    public SceneManager(@NotNull Executor executor, @NotNull Set<Class<? extends Scene>> sceneTypes, int numThreads) {
        this.executor = Objects.requireNonNull(executor);
        this.mappedScenes = buildSceneMap(Set.copyOf(sceneTypes));
        this.threadDispatcher = ThreadDispatcher.of(ThreadProvider.counter(), numThreads);
    }

    public SceneManager(@NotNull Executor executor, @NotNull Set<Class<? extends Scene>> sceneTypes) {
        this(executor, sceneTypes, Runtime.getRuntime().availableProcessors());
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends Scene>, SceneEntry> buildSceneMap(Collection<Class<? extends Scene>> sceneTypes) {
        Map.Entry<Class<? extends Scene>, SceneEntry>[] entries = new Map.Entry[sceneTypes.size()];
        int i = 0;
        for (Class<? extends Scene> sceneType : sceneTypes) {
            entries[i++] = Map.entry(sceneType, new SceneEntry(Collections.newSetFromMap(new ConcurrentHashMap<>()),
                new ReentrantLock()));
        }

        return Map.ofEntries(entries);
    }

    /**
     * Should be called once with a representative {@link PlayerView} when said player disconnects from the server. This
     * will update state and remove the player from their old {@link Scene}.
     *
     * @param playerView the player to disconnect
     */
    public void handleDisconnect(@NotNull PlayerView playerView) {
        PlayerViewImpl view = (PlayerViewImpl) playerView;

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
     * Note that this method will <i>leave</i> any players from the scene when it has shut down, but these players will
     * not be sent anywhere. That is the responsibility of {@code playerCallback}, a {@link CompletableFuture}-returning
     * function. When its future is complete (exceptionally or otherwise), the scene will be fully shut down by calling
     * {@link Scene#shutdown()}.
     *
     * @param scene          the scene to remove
     * @param playerCallback the callback to run with the set of all players that were previously in {@code scene}
     */
    public void removeScene(@NotNull Scene scene,
        @NotNull Function<? super @NotNull Set<@NotNull PlayerView>, ? extends @NotNull CompletableFuture<?>> playerCallback) {
        SceneEntry entry = mappedScenes.get(scene.getClass());
        if (entry == null) {
            return;
        }

        Set<Scene> scenes = entry.scenes;
        if (!scenes.remove(scene)) {
            return;
        }

        threadDispatcher.deletePartition(scene);

        scene.getAcquirable().async(self -> {
            if (self.isShutdown()) {
                return;
            }

            Set<PlayerView> players = self.players();
            lockPlayers(players, true);

            try {
                self.leave(players);

                for (PlayerView playerView : players) {
                    ((PlayerViewImpl) playerView).updateCurrentScene(null);
                }
            } finally {
                unlockPlayers(players);
            }

            playerCallback.apply(players).whenComplete((ignored1, ignored2) -> {
                scene.getAcquirable().sync(Scene::shutdown);
            });
        });
    }

    /**
     * Retrieves all scenes of a specified type. The resulting set is unmodifiable and will not change even if new
     * scenes are created after the invocation of this method. None of the scenes are acquired.
     *
     * @param sceneType the scene type to look up
     * @param <T>       the type of scene to search for
     * @return a set of scenes of the specified type; or {@code null} if such a type has not been registered; the set
     * may additionally be empty, in which case scenes of that type <i>may</i> exist but none currently do
     */
    @SuppressWarnings("unchecked")
    public <T extends Scene> @Nullable @Unmodifiable Set<T> typed(@NotNull Class<T> sceneType) {
        SceneEntry entry = mappedScenes.get(sceneType);
        if (entry == null) {
            return null;
        }

        return (Set<T>) Set.copyOf(entry.scenes);
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
        SceneEntry entry = mappedScenes.get(join.targetType());
        if (entry == null) {
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
            if (tryJoinScenes(entry.scenes, join)) {
                return JoinResult.JOINED;
            }

            synchronized (entry.creationLock) {
                //another thread may have created a scene we can join before we locked
                if (tryJoinScenes(entry.scenes, join)) {
                    return JoinResult.JOINED;
                }

                if (!join.canCreateNewScene()) {
                    return JoinResult.CANNOT_PROVISION;
                }

                Scene newScene = createAndJoinNewScene(join);

                entry.scenes.add(newScene);
                threadDispatcher.createPartition(newScene);
                threadDispatcher.updateElement(newScene, newScene);

                return JoinResult.JOINED;
            }
        }, executor).whenComplete((ignored, ignored1) -> {
            unlockPlayers(join.players());
        });
    }

    private boolean tryJoinScenes(Set<Scene> scenes, Join<?> join) {
        for (Scene scene : scenes) {
            if (tryJoinScene(scene, join)) {
                return true;
            }
        }

        return false;
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

    /**
     * Registers a new {@link Tickable}, that will be ticked in the same context as the provided {@link Scene}. That is,
     * the same thread that is used to tick the scene will also be used to tick {@code tickable}. The tickable will be
     * removed when the scene is removed.
     * <p>
     * If {@code context} is not a scene currently in the manager, this method will do nothing.
     *
     * @param context  the scene context
     * @param tickable the tickable to be bound to the scene
     */
    public void addTickable(@NotNull Scene context, @NotNull Tickable tickable) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(tickable);
        threadDispatcher.updateElement(tickable, context);
    }

    /**
     * Manually removes a {@link Tickable}. If this was never added using
     * {@link SceneManager#addTickable(Scene, Tickable)}, this method will do nothing.
     *
     * @param tickable the tickable to remove
     */
    public void removeTickable(@NotNull Tickable tickable) {
        Objects.requireNonNull(tickable);
        threadDispatcher.removeElement(tickable);
    }

    private <T extends Scene> T createAndJoinNewScene(Join<T> join) {
        T scene = join.createNewScene();
        leaveOldScenes(join, scene);

        //not necessary to acquire, scene was just created but is not ticking yet
        join.join(scene);

        return scene;
    }

    private <T extends Scene> boolean tryJoinScene(Scene scene, Join<T> join) {
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
