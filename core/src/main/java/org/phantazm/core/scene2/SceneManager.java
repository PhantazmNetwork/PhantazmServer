package org.phantazm.core.scene2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.Acquired;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewImpl;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * SceneManager is used to fulfill requests by one or more players to join {@link Scene} objects. This class also
 * manages the lifecycle of every Scene object; creation, ticking, and eventually removal. Several different utility
 * methods, such as {@link SceneManager#getCurrentScene(PlayerView)}, are also provided to make working with scenes
 * easier.
 * <p>
 * This class is intended to be used as a singleton. The SceneManager instance for this application can be obtained
 * through calling {@link Global#instance()} after ensuring {@link Global#init(Executor, Set, PlayerViewProvider, int)}
 * has been called exactly <i>once</i>.
 * <p>
 * <h2>Thread Safety</h2>
 * Unless otherwise indicated, all methods declared on this class are completely thread-safe.
 */
public final class SceneManager {
    /**
     * A container class which holds the global SceneManager and provides access to its initialization through
     * {@link Global#init(Executor, Set, PlayerViewProvider, int)}.
     */
    public static final class Global {
        private static final Object GLOBAL_INITIALIZE_LOCK = new Object();

        private static SceneManager instance;

        /**
         * Initializes the global {@link SceneManager}. This should be called only once, after
         * {@link MinecraftServer#init()} but optionally before starting. Calling it more than once will result in an
         * {@link IllegalStateException}.
         *
         * @param executor     the executor to be used for join request fulfillment by the global SceneManager
         * @param sceneTypes   the types of scene recognized by the global SceneManager
         * @param viewProvider the {@link PlayerViewProvider} used to resolve PlayerView instances
         * @param numThreads   the number of threads dedicated to ticking {@link Scene} instances
         */
        public static void init(@NotNull Executor executor, @NotNull Set<Class<? extends Scene>> sceneTypes,
            @NotNull PlayerViewProvider viewProvider, int numThreads) {
            Objects.requireNonNull(viewProvider);

            synchronized (GLOBAL_INITIALIZE_LOCK) {
                if (instance != null) {
                    throw new IllegalStateException("The global SceneManager has already been initialized");
                }

                SceneManager manager = new SceneManager(executor, sceneTypes, numThreads);

                MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, disconnectEvent -> {
                    manager.handleDisconnect(viewProvider.fromPlayer(disconnectEvent.getPlayer()));
                });

                MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, manager::handleLogin);

                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    manager.tick(System.currentTimeMillis());
                }, TaskSchedule.immediate(), TaskSchedule.nextTick());

                instance = manager;
            }
        }

        /**
         * Gets the global {@link SceneManager} instance. If this has not already been initialized through a call to
         * {@link Global#init(Executor, Set, PlayerViewProvider, int)}, a {@link IllegalStateException} will be thrown.
         *
         * @return the global {@link SceneManager}
         */
        public static @NotNull SceneManager instance() {
            SceneManager instance = Global.instance;
            if (instance == null) {
                throw new IllegalStateException("The global SceneManager has not yet been initialized");
            }

            return instance;
        }
    }

    /**
     * Represents the status of an attempted join for one or more players.
     */
    public enum JoinStatus {
        /**
         * The join operation completed successfully. The scene was able to accept all necessary players.
         */
        JOINED,

        /**
         * No players joined because the Join did not have any.
         */
        EMPTY_PLAYERS,

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

    /**
     * Encapsulates the result of a successful or failed attempt to join a scene contained in this manager.
     *
     * @param status the {@link JoinStatus} containing the status of the operation
     * @param scene  the scene that was joined; is {@code null} if {@code status != JoinStatus.JOINED}, non-null
     *               otherwise
     * @param <T>    the type of scene that was joined
     */
    public record JoinResult<T extends Scene>(@NotNull JoinStatus status,
        T scene) {
        private static final JoinResult<?> EMPTY_PLAYERS = new JoinResult<>(JoinStatus.EMPTY_PLAYERS, null);

        private static final JoinResult<?> UNRECOGNIZED_TYPE = new JoinResult<>(JoinStatus.UNRECOGNIZED_TYPE, null);

        private static final JoinResult<?> ALREADY_JOINING = new JoinResult<>(JoinStatus.ALREADY_JOINING, null);

        private static final JoinResult<?> CANNOT_PROVISION = new JoinResult<>(JoinStatus.CANNOT_PROVISION, null);

        public JoinResult {
            Objects.requireNonNull(status);
            if (status == JoinStatus.JOINED) {
                Objects.requireNonNull(scene);
            } else if (scene != null) {
                throw new IllegalArgumentException("Cannot specify a scene for a JoinStatus other than JOINED");
            }
        }

        /**
         * Returns a JoinResult with status {@link JoinStatus#EMPTY_PLAYERS}.
         *
         * @param <T> the type of scene that was joined
         * @return a JoinResult with status {@link JoinStatus#EMPTY_PLAYERS}
         */
        @SuppressWarnings("unchecked")
        public static <T extends Scene> @NotNull JoinResult<T> emptyPlayers() {
            return (JoinResult<T>) EMPTY_PLAYERS;
        }

        /**
         * Returns a JoinResult with status {@link JoinStatus#UNRECOGNIZED_TYPE}.
         *
         * @param <T> the type of scene that was joined
         * @return a JoinResult with status {@link JoinStatus#UNRECOGNIZED_TYPE}
         */
        @SuppressWarnings("unchecked")
        public static <T extends Scene> @NotNull JoinResult<T> unrecognizedType() {
            return (JoinResult<T>) UNRECOGNIZED_TYPE;
        }

        /**
         * Returns a JoinResult with status {@link JoinStatus#ALREADY_JOINING}.
         *
         * @param <T> the type of scene that was joined
         * @return a JoinResult with status {@link JoinStatus#ALREADY_JOINING}
         */
        @SuppressWarnings("unchecked")
        public static <T extends Scene> @NotNull JoinResult<T> alreadyJoining() {
            return (JoinResult<T>) ALREADY_JOINING;
        }

        /**
         * Returns a JoinResult with status {@link JoinStatus#CANNOT_PROVISION}.
         *
         * @param <T> the type of scene that was joined
         * @return a JoinResult with status {@link JoinStatus#CANNOT_PROVISION}
         */
        @SuppressWarnings("unchecked")
        public static <T extends Scene> @NotNull JoinResult<T> cannotProvision() {
            return (JoinResult<T>) CANNOT_PROVISION;
        }

        /**
         * Creates a successful JoinResult with status {@link JoinStatus#JOINED} and a given {@link Scene}.
         *
         * @param scene the scene that was joined
         * @param <T>   the type of scene that was joined
         * @return a JoinResult with status {@link JoinStatus#JOINED} and a non-null Scene
         */
        public static <T extends Scene> @NotNull JoinResult<T> joined(@NotNull T scene) {
            return new JoinResult<>(JoinStatus.JOINED, scene);
        }

        /**
         * Returns {@code true} if this JoinResult encapsulates a successful join attempt (and therefore, has a non-null
         * scene). Returns {@code false} otherwise.
         *
         * @return {@code true} if this JoinResult encapsulates a successful join attempt; {@code false} otherwise
         */
        public boolean successful() {
            return status == JoinStatus.JOINED;
        }
    }

    private record SceneEntry(Set<Scene> scenes,
        Object creationLock) {
    }

    private final Executor executor;
    private final Map<Class<? extends Scene>, SceneEntry> mappedScenes;
    private final ThreadDispatcher<Scene> threadDispatcher;

    private volatile Function<? super Player, ? extends Join<? extends InstanceScene>> requestMapper;

    private SceneManager(@NotNull Executor executor, @NotNull Set<Class<? extends Scene>> sceneTypes, int numThreads) {
        this.executor = Objects.requireNonNull(executor);
        this.mappedScenes = buildSceneMap(Set.copyOf(sceneTypes));
        this.threadDispatcher = ThreadDispatcher.of(ThreadProvider.counter(), numThreads);
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends Scene>, SceneEntry> buildSceneMap(Collection<Class<? extends Scene>> sceneTypes) {
        Map.Entry<Class<? extends Scene>, SceneEntry>[] entries = new Map.Entry[sceneTypes.size()];
        int i = 0;
        for (Class<? extends Scene> sceneType : sceneTypes) {
            entries[i++] = Map.entry(sceneType, new SceneEntry(Collections.newSetFromMap(new ConcurrentHashMap<>()),
                new Object()));
        }

        return Map.ofEntries(entries);
    }

    private void handleDisconnect(@NotNull PlayerView playerView) {
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

    private void handleLogin(@NotNull PlayerLoginEvent loginEvent) {
        if (loginEvent.getSpawningInstance() != null) {
            return;
        }

        Player player = loginEvent.getPlayer();

        Function<? super Player, ? extends Join<? extends InstanceScene>> mapper = this.requestMapper;
        if (mapper == null) {
            loginEvent.setCancelled(true);
            return;
        }

        Join<? extends InstanceScene> join = mapper.apply(player);
        JoinResult<? extends InstanceScene> result = joinScene(join).join();
        if (!result.successful()) {
            loginEvent.setCancelled(true);
            return;
        }

        InstanceScene scene = result.scene();
        loginEvent.setSpawningInstance(scene.instance());
    }

    /**
     * Sets the function that will be used to construct {@link Join} instances in response to player login. Such Join
     * objects must have a scene type assignable to {@link InstanceScene}, because it is necessary to set the player's
     * spawning instance on login.
     * <p>
     * If set to {@code null}, players will be unable to join the server. The login hook is set to null by default, so
     * to have the server be accessible at all, this needs to be set.
     *
     * @param requestMapper the function used to create Join objects for players in response to login events
     */
    public void setLoginHook(@Nullable Function<? super @NotNull Player, ? extends @NotNull Join<? extends InstanceScene>> requestMapper) {
        this.requestMapper = requestMapper;
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

        scene.getAcquirable().sync(self -> {
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
     * @param <T>  the type of scene
     * @return a {@link CompletableFuture} containing a {@link JoinResult} indicating the success or failure of the
     * join, as well as the scene that was joined (if successful)
     */
    public <T extends Scene> @NotNull CompletableFuture<JoinResult<T>> joinScene(@NotNull Join<T> join) {
        SceneEntry entry = mappedScenes.get(join.targetType());
        if (entry == null) {
            return CompletableFuture.completedFuture(JoinResult.unrecognizedType());
        }

        Set<PlayerView> players = join.players();
        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(JoinResult.emptyPlayers());
        }

        if (!lockPlayers(players, false)) {
            return CompletableFuture.completedFuture(JoinResult.alreadyJoining());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                T joinedScene = tryJoinScenes(entry.scenes, join);
                if (joinedScene != null) {
                    return JoinResult.joined(joinedScene);
                }

                synchronized (entry.creationLock) {
                    joinedScene = tryJoinScenes(entry.scenes, join);
                    if (joinedScene != null) {
                        return JoinResult.joined(joinedScene);
                    }

                    if (!join.canCreateNewScene()) {
                        return JoinResult.cannotProvision();
                    }

                    T newScene = createAndJoinNewScene(join);

                    entry.scenes.add(newScene);
                    threadDispatcher.createPartition(newScene);
                    threadDispatcher.updateElement(newScene, newScene);

                    return JoinResult.joined(newScene);
                }
            } finally {
                unlockPlayers(players);
            }
        }, executor);
    }

    private <T extends Scene> T tryJoinScenes(Set<Scene> scenes, Join<T> join) {
        for (Scene scene : scenes) {
            T joinedScene = tryJoinScene(scene, join);
            if (joinedScene != null) {
                return joinedScene;
            }
        }

        return null;
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

    private <T extends Scene> T tryJoinScene(Scene scene, Join<T> join) {
        Acquirable<? extends Scene> sceneAcquirable = scene.getAcquirable();
        T castScene = join.targetType().cast(scene);

        Acquired<? extends Scene> acquired = sceneAcquirable.lock();
        try {
            if (!castScene.joinable() || !join.matches(castScene)) {
                return null;
            }

            leaveOldScenes(join, castScene);
            join.join(castScene);
        } finally {
            acquired.unlock();
        }

        return castScene;
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


    private void tick(long time) {
        threadDispatcher.updateAndAwait(time);
        threadDispatcher.refreshThreads(System.currentTimeMillis() - time);
    }
}
