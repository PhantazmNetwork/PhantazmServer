package org.phantazm.core.scene2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerTablistEvent;
import net.minestom.server.thread.Acquired;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewImpl;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * SceneManager is used to fulfill requests by one or more players to join {@link Scene} objects. This class also
 * manages the lifecycle of every Scene object; creation, ticking, and eventually removal. Several different utility
 * methods, such as {@link SceneManager#currentScene(PlayerView)}, are also provided to make working with scenes
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
        public static void init(@NotNull Executor executor, @NotNull Set<@NotNull Class<? extends Scene>> sceneTypes,
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
                MinecraftServer.getGlobalEventHandler().addListener(PlayerTablistEvent.class, manager::handleTablist);
                MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, manager::handleSpawn);

                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    manager.tick(System.currentTimeMillis());
                }, TaskSchedule.immediate(), TaskSchedule.nextTick());

                instance = manager;
            }
        }

        /**
         * Gets the global {@link SceneManager} instance. If this has not already been initialized through a call to
         * {@link Global#init(Executor, Set, PlayerViewProvider, int)}, an {@link IllegalStateException} will be
         * thrown.
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
     * A key for a specific type of Join. See {@link SceneManager#joinKey(Class, String)}.
     *
     * @param <T> the type of scene this key joins
     */
    public static class Key<T extends Scene> {
        private final Class<T> type;
        private final String name;
        private final int hash;

        private Key(Class<T> type, String name) {
            this.type = type;
            this.name = name;
            this.hash = computeHash(type, name);
        }

        private static int computeHash(Class<?> clazz, String id) {
            return 31 * (31 + clazz.hashCode()) + id.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            if (obj instanceof Key<?> other) {
                return type.equals(other.type) && name.equals(other.name);
            }

            return false;
        }

        @Override
        public String toString() {
            return "Key[" + type.getSimpleName() + (name.isEmpty() ? "]" : (", " + name + "]"));
        }
    }

    /**
     * A function for creating a {@link Join} from a set of players.
     *
     * @param <T> the type of scene to join
     */
    public interface JoinFunction<T extends Scene> extends
        Function<@NotNull Set<@NotNull PlayerView>, @NotNull Join<T>> {
        @NotNull Class<T> type();
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
    private final Map<Key<?>, JoinFunction<?>> functionMap;

    private volatile Function<? super Player, ? extends LoginJoin<? extends InstanceScene>> requestMapper;

    private SceneManager(Executor executor, Set<Class<? extends Scene>> sceneTypes, int numThreads) {
        this.executor = Objects.requireNonNull(executor);
        this.mappedScenes = buildSceneMap(Set.copyOf(sceneTypes));
        this.threadDispatcher = ThreadDispatcher.of(ThreadProvider.counter(), numThreads);
        this.functionMap = new ConcurrentHashMap<>();
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

        Lock lock = view.joinLock();
        lock.lock();
        try {
            view.currentScene().ifPresent(scene -> scene.getAcquirable().sync(self -> {
                    self.leave(Set.of(view));
                })
            );

            view.updateCurrentScene(null);
        } finally {
            lock.unlock();
        }
    }

    /**
     * A specialization of {@link Join} that is required for joining during <i>login</i>; that is, when the player first
     * signs onto the server. Join instances will need to implement this interface if they are to be used as the return
     * value of the function passed to {@link SceneManager#setLoginHook(Function)}.
     *
     * @param <T> the type of scene to join, which must subclass {@link InstanceScene}
     */
    public interface LoginJoin<T extends InstanceScene> extends Join<T> {
        /**
         * A function called by the {@link SceneManager} as triggered by the first {@link PlayerSpawnEvent} made some
         * time after a player logs into the server. If this method is called, it means that the player was able to
         * successfully join a scene using this LoginJoin. Therefore, this method should update the scene that was
         * previously passed to {@link LoginJoin#join(Scene)}.
         * <p>
         * Note: It is necessary to synchronize on the stored scene using the Acquirable API, as unlike
         * {@link Join#join(Scene)}, no lock on it is guaranteed to be held when this method is called.
         *
         * @see SceneManager#setLoginHook(Function)
         */
        void postSpawn();

        /**
         * Called before {@link LoginJoin#postSpawn()}, but before after the initial call to
         * {@link LoginJoin#join(Scene)}. This is responsible for sending tablist packets, as appropriate, so that the
         * players in the spawning scene are able to see the joining player. The player's instance has still not been
         * set yet, so many methods on it will not work.
         *
         * @param tablistRecipients a modifiable list, to which players should be added in order for them to receive a
         *                          tablist packet
         */
        void updateTablist(@NotNull List<@NotNull Player> tablistRecipients);
    }

    private final Map<UUID, LoginJoin<?>> joinRequestMap = new ConcurrentHashMap<>();

    private void handleLogin(@NotNull PlayerLoginEvent loginEvent) {
        Player player = loginEvent.getPlayer();

        Function<? super Player, ? extends LoginJoin<? extends InstanceScene>> mapper = this.requestMapper;
        if (mapper == null) {
            loginEvent.setCancelled(true);
            return;
        }

        LoginJoin<?> loginJoin = Objects.requireNonNull(mapper.apply(player));
        JoinResult<? extends InstanceScene> result = joinScene(loginJoin).join();
        if (!result.successful()) {
            loginEvent.setCancelled(true);
            return;
        }

        joinRequestMap.put(player.getUuid(), loginJoin);
        loginEvent.setSpawningInstance(result.scene().instance());
    }

    private void handleTablist(@NotNull PlayerTablistEvent tablistEvent) {
        if (!tablistEvent.isFirstSpawn()) {
            return;
        }

        LoginJoin<?> loginJoin = joinRequestMap.get(tablistEvent.getPlayer().getUuid());
        if (loginJoin != null) {
            loginJoin.updateTablist(tablistEvent.tablistAddRecipients());
        }
    }

    private void handleSpawn(@NotNull PlayerSpawnEvent spawnEvent) {
        if (!spawnEvent.isFirstSpawn()) {
            return;
        }

        LoginJoin<?> loginJoin = joinRequestMap.remove(spawnEvent.getPlayer().getUuid());
        if (loginJoin != null) {
            loginJoin.postSpawn();
        }
    }

    private void validateType(Class<?> type) {
        Objects.requireNonNull(type);

        if (!mappedScenes.containsKey(type)) {
            throw new IllegalArgumentException("attempted to create a key for which no scene exists");
        }
    }

    public @NotNull <T extends Scene> Key<T> joinKey(@NotNull Class<T> type) {
        validateType(type);
        return new Key<>(type, "");
    }

    public @NotNull <T extends Scene> Key<T> joinKey(@NotNull Class<T> type, @NotNull String name) {
        validateType(type);
        Objects.requireNonNull(name);
        return new Key<>(type, name);
    }

    public @NotNull <T extends Scene> JoinFunction<T> joinFunction(@NotNull Class<T> type,
        @NotNull Function<@NotNull Set<@NotNull PlayerView>, @NotNull Join<T>> function) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(function);

        if (!Scene.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("type not assignable to Scene");
        }

        return new JoinFunction<>() {
            @Override
            public @NotNull Class<T> type() {
                return type;
            }

            @Override
            public @NotNull Join<T> apply(@NotNull Set<@NotNull PlayerView> playerViews) {
                return Objects.requireNonNull(function.apply(playerViews));
            }
        };
    }

    public <T extends Scene> void registerJoinFunction(@NotNull Key<T> key, @NotNull JoinFunction<T> function) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(function);

        if (!key.type.equals(function.type())) {
            throw new IllegalArgumentException("mismatch between Key type and JoinFunction type");
        }

        functionMap.put(key, function);
    }

    @SuppressWarnings("unchecked")
    public <T extends Scene> @NotNull CompletableFuture<JoinResult<T>> joinScene(@NotNull Key<T> key,
        @NotNull Set<@NotNull PlayerView> players) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(players);

        JoinFunction<T> function = (JoinFunction<T>) functionMap.get(key);
        if (function == null) {
            throw new IllegalArgumentException("no join function mapped to " + key);
        }

        return joinScene(function.apply(players));
    }

    /**
     * Sets the function that will be used to construct {@link LoginJoin} instances in response to player login. Such
     * Join objects must have a scene type assignable to {@link InstanceScene}, because it is necessary to set the
     * player's spawning instance on login.
     * <p>
     * If set to {@code null}, players will be unable to join the server. The login hook is set to null by default, so
     * to have the server be accessible at all, this needs to be set.
     * <p>
     * Successfully fulfilling a LoginJoin requires special support from scenes, as many methods on {@link Player} will
     * not work (such as {@link Player#teleport(Pos)}). Therefore, fulfilling a LoginJoin is split into three parts:
     * <ol>
     *     <li>The initial login, where a suitable scene is found and joined. The scene must be made aware that the
     *     player is just logging in, so that it will not call any methods that are unusable at this point.</li>
     *     <li>Tablist update, where the recipients of tablist packets for the joining player are determined.</li>
     *     <li>The post-spawn, which is invoked at a later point once the player has been fully initialized. The full
     *     scope of Player-related methods may now be called.</li>
     * </ol>
     * <p>
     * The first phase is triggered by {@link PlayerLoginEvent} to locate a suitable scene using the LoginJoin provided
     * by the login hook function. If a scene is successfully found, the LoginJoin instance is <i>saved</i>. LoginJoin
     * implementations are expected to save the scene that was passed to {@link LoginJoin#join(Scene)} so that necessary
     * methods to update player state can be called in the next two phases.
     * <p>
     * At some point later, a {@link PlayerTablistEvent} is triggered and {@link LoginJoin#updateTablist(List)} is
     * called to determine which players should see the joining player in the tablist. Generally speaking, this is only
     * players in the scene that the player is joining, but different LoginJoin implementations will have different
     * rules.
     * <p>
     * Finally, {@link PlayerSpawnEvent} is called to initially spawn in the player. At this point, the saved
     * LoginJoin instance is removed and the {@link LoginJoin#postSpawn()} method is called, which will fully add the
     * player to the scene, as they now have their instance properly set.
     *
     * @param requestMapper the function used to create Join objects for players in response to login events
     */
    public void setLoginHook(@Nullable Function<? super @NotNull Player, ? extends @NotNull LoginJoin<? extends InstanceScene>> requestMapper) {
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

        Acquired<? extends Scene> acquired = scene.getAcquirable().lock();

        Set<PlayerView> players = null;
        boolean playersLocked = false;

        try {
            try {
                scene.preShutdown();

                players = scene.players();
                playersLocked = lockPlayers(players, true);

                scene.leave(players);
            } finally {
                acquired.unlock();
            }

            //while the players lock is held, set their current scene to null
            for (PlayerView playerView : players) {
                ((PlayerViewImpl) playerView).updateCurrentScene(null);
            }
        } finally {
            if (playersLocked) {
                unlockPlayers(players);
            }
        }

        playerCallback.apply(players).whenComplete((ignored1, ignored2) -> {
            scene.getAcquirable().sync(Scene::shutdown);
        });
    }

    /**
     * Returns the set of all valid scene types for this manager.
     *
     * @return the set of all valid scene types for this manager
     */
    public @NotNull @Unmodifiable Set<Class<? extends Scene>> types() {
        return mappedScenes.keySet();
    }

    /**
     * Retrieves all scenes of a specified type. The resulting set is an <i>unmodifiable view</i>; it cannot be modified
     * by calling code, but it may change as new scenes are added or removed by this manager. This method will perform
     * an exact match; subclasses of {@code sceneType} will not be included.
     *
     * @param sceneType the exact scene type to look up
     * @param <T>       the type of scene to search for
     * @return a set of scenes of the specified type; or {@code null} if such a type has not been registered; the set
     * may additionally be empty, in which case scenes of that type <i>may</i> exist in the future but none currently
     * do
     */
    @SuppressWarnings("unchecked")
    public <T extends Scene> @UnmodifiableView Set<T> typed(@NotNull Class<T> sceneType) {
        SceneEntry entry = mappedScenes.get(sceneType);
        if (entry == null) {
            return null;
        }

        return (Set<T>) Collections.unmodifiableSet(entry.scenes);
    }

    /**
     * Retrieves the number of scenes that exactly match a specified type. If the type has not been registered,
     * {@code -1} is returned.
     * <p>
     * If called from within {@link Join#canCreateNewScene(SceneManager)}, this SceneManager should ensure
     *
     * @param sceneType the type of scene to search for
     * @param <T>       the type of scene to search for
     * @return the number of scenes that are currently active (not shut down) of the specified type, or {@code -1} if no
     * such scene type has been registered
     */
    public <T extends Scene> int amount(@NotNull Class<T> sceneType) {
        SceneEntry entry = mappedScenes.get(sceneType);
        if (entry == null) {
            return -1;
        }

        return entry.scenes.size();
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

        return CompletableFuture.supplyAsync(() -> {
            if (!lockPlayers(players, false)) {
                return JoinResult.alreadyJoining();
            }

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

                    if (!join.canCreateNewScene(this)) {
                        return JoinResult.cannotProvision();
                    }

                    T newScene = createAndJoinNewScene(join);

                    entry.scenes.add(newScene);
                    threadDispatcher.createPartition(newScene);

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

        List<Lock> acquiredLocks = force ? null : new ArrayList<>(players.size());
        for (PlayerView playerView : players) {
            Lock lock = ((PlayerViewImpl) playerView).joinLock();
            if (force) {
                lock.lock();
                continue;
            }

            if (lock.tryLock()) {
                acquiredLocks.add(lock);
                continue;
            }

            for (Lock acquired : acquiredLocks) {
                acquired.unlock();
            }

            return false;
        }

        return true;
    }

    private void unlockPlayers(Iterable<PlayerView> players) {
        for (PlayerView playerView : players) {
            ((PlayerViewImpl) playerView).joinLock().unlock();
        }
    }

    /**
     * Gets an Optional that may contain the current scene the player is in, or {@code null} if the player does not
     * belong to an existing scene.
     *
     * @param playerView the player to check
     * @return an Optional containing the current scene
     */
    public @NotNull Optional<Scene> currentScene(@NotNull PlayerView playerView) {
        return ((PlayerViewImpl) playerView).currentScene();
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

        Lock lock = view.joinLock();
        lock.lock();
        try {
            Optional<Scene> scene = view.currentScene();
            if (scene.isEmpty()) {
                return;
            }

            consumer.accept(view, scene.get());
        } finally {
            lock.unlock();
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
        T scene = join.createNewScene(this);
        leaveOldScenes(join, scene);

        //not necessary to acquire, scene was just created but is not ticking yet
        join.join(scene);

        return scene;
    }

    private <T extends Scene> T tryJoinScene(Scene scene, Join<T> join) {
        T castScene = join.targetType().cast(scene);

        Acquired<? extends Scene> acquired = castScene.getAcquirable().lock();
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
