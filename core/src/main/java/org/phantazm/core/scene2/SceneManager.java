package org.phantazm.core.scene2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.*;
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
import org.phantazm.core.event.scene.SceneCreationEvent;
import org.phantazm.core.event.scene.SceneJoinEvent;
import org.phantazm.core.event.scene.SceneShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(SceneManager.class);

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
                MinecraftServer.getGlobalEventHandler().addListener(PlayerTablistRemoveEvent.class,
                    manager::handlePostDisconnect);

                MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, manager::handleLogin);
                MinecraftServer.getGlobalEventHandler().addListener(PlayerTablistShowEvent.class, manager::handleTablist);
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
     * A key for a specific type of {@link Join}. Used by some methods on SceneManager as an alternative to manually
     * creating Join instances every time they are needed. The constructor for this class is private, but instances can
     * be obtained by calling {@link SceneManager#joinKey(Class, String)} or an overload.
     * <p>
     * Instances should generally be stored in {@code public static} fields and referenced as-needed by other code.
     *
     * @param <T> the type of scene this key joins
     * @see SceneManager#joinKey(Class, String)
     * @see SceneManager#registerJoinFunction(Key, JoinFunction)
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
        /**
         * The type of scene this function will join.
         *
         * @return the type of scene this function will join
         */
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
        CANNOT_PROVISION,

        /**
         * Join status used in {@link SceneJoinEvent} to indicate an internal error occurred. Players may have been
         * moved, some state may have changed. This typically indicates a bug.
         */
        INTERNAL_ERROR
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
        private static final JoinResult<?> INTERNAL_ERROR = new JoinResult<>(JoinStatus.INTERNAL_ERROR, null);

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
    private volatile Function<? super Set<PlayerView>, ? extends CompletableFuture<?>> globalFallback;

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

    private record LeaveEntry(Scene scene,
        Set<Player> left) {
    }

    private final Map<UUID, LeaveEntry> leaveEntryMap = new ConcurrentHashMap<>();

    private void handleDisconnect(@NotNull PlayerView playerView) {
        PlayerViewImpl view = (PlayerViewImpl) playerView;
        view.tagHandler().clearTags();

        Lock lock = view.joinLock();
        lock.lock();
        try {
            Optional<Scene> currentSceneOptional = view.currentScene();
            if (currentSceneOptional.isEmpty()) {
                return;
            }

            Scene scene = currentSceneOptional.get();
            Acquired<? extends Scene> acquired = scene.getAcquirable().lock();
            try {
                Scene self = acquired.get();

                Set<Player> left = unwrapMany(self.leave(Set.of(view)), HashSet::new);
                view.updateCurrentScene(null);

                leaveEntryMap.put(view.getUUID(), new LeaveEntry(scene, left));
            } finally {
                acquired.unlock();
            }
        } finally {
            lock.unlock();
        }
    }

    private void handlePostDisconnect(PlayerTablistRemoveEvent event) {
        Player player = event.getPlayer();
        LeaveEntry left = leaveEntryMap.remove(player.getUuid());
        if (left == null) {
            return;
        }

        event.setBroadcastTablistRemoval(false);
        left.scene.getAcquirable().sync(self -> self.postLeave(left.left));
    }

    private static <T extends Collection<Player>> T unwrapMany(Set<? extends PlayerView> playerViews,
        IntFunction<? extends T> function) {
        T collection = function.apply(playerViews.size());
        for (PlayerView playerView : playerViews) {
            Player playerReference = ((PlayerViewImpl) playerView).reference();
            if (playerReference != null) {
                collection.add(playerReference);
                continue;
            }

            if (Entity.getEntity(playerView.getUUID()) instanceof Player player) {
                collection.add(player);
            }
        }

        return collection;
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
         * A function called by the {@link SceneManager} when triggered by the first {@link PlayerSpawnEvent} made some
         * time after a player logs onto the server. If this method is called, it means that the player was able to
         * successfully join a scene using this LoginJoin. Therefore, this method should update {@code scene} as
         * necessary to ensure the player has been fully added.
         * <p>
         * No lock will be held on the provided scene.
         *
         * @param scene the scene that was previously passed to {@link Join#join(Scene)}
         * @see SceneManager#setLoginHook(Function)
         */
        void postSpawn(@NotNull T scene);

        /**
         * Called before {@link LoginJoin#postSpawn(InstanceScene)}, but after the initial call to
         * {@link LoginJoin#join(Scene)}. This is responsible for determining which players should see the joining
         * player.
         * <p>
         * No lock will be held on the provided scene.
         *
         * @param scene            the scene that was previously passed to {@link Join#join(Scene)}
         * @param tablistShowEvent an event object which can be modified to control which players may see the joining
         *                         player in the tablist
         */
        void updateLoginTablist(@NotNull T scene, @NotNull PlayerTablistShowEvent tablistShowEvent);
    }

    private record LoginEntry<T extends InstanceScene>(LoginJoin<T> join,
        T scene) {
        private void updateTablist(PlayerTablistShowEvent event) {
            join.updateLoginTablist(scene, event);
        }

        private void postSpawn() {
            join.postSpawn(scene);
        }

        private static <T extends InstanceScene> LoginEntry<T> of(LoginJoin<T> join,
            JoinResult<? extends InstanceScene> result) {
            return new LoginEntry<>(join, join.targetType().cast(result.scene));
        }
    }

    private final Map<UUID, LoginEntry<?>> joinRequestMap = new ConcurrentHashMap<>();

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

        joinRequestMap.put(player.getUuid(), LoginEntry.of(loginJoin, result));
        loginEvent.setSpawningInstance(result.scene().instance());
    }

    private void handleTablist(@NotNull PlayerTablistShowEvent tablistEvent) {
        LoginEntry<?> entry = joinRequestMap.get(tablistEvent.getPlayer().getUuid());
        if (entry != null) {
            entry.updateTablist(tablistEvent);
        }
    }

    private void handleSpawn(@NotNull PlayerSpawnEvent spawnEvent) {
        if (!spawnEvent.isFirstSpawn()) {
            return;
        }

        LoginEntry<?> entry = joinRequestMap.remove(spawnEvent.getPlayer().getUuid());
        if (entry != null) {
            entry.postSpawn();
        }
    }

    /**
     * Creates a new {@link Key} of type {@code type} and name {@code name}.
     *
     * @param type the type of {@link Scene} this Key will be associated with
     * @param name the name of the key, used to disambiguate when there are multiple keys with the same underlying type
     * @param <T>  the type of Scene this key is for
     * @return a new Key instance
     */
    public static @NotNull <T extends Scene> Key<T> joinKey(@NotNull Class<T> type, @NotNull String name) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
        return new Key<>(type, name);
    }

    /**
     * Creates a new {@link Key} of type {@code type} and whose name is an empty string. Useful if it is anticipated
     * that there will only be one Key necessary for a given kind of scene.
     *
     * @param type the type of {@link Scene} this Key will be associated with
     * @param <T>  the type of Scene this key is for
     * @return a new Key instance
     */
    public static @NotNull <T extends Scene> Key<T> joinKey(@NotNull Class<T> type) {
        Objects.requireNonNull(type);
        return new Key<>(type, "");
    }

    /**
     * Creates a new {@link JoinFunction} implementation.
     *
     * @param type     the type of the JoinFunction
     * @param function a {@link Function} which accepts a set of {@link PlayerView} instances and returns a new
     *                 {@link Join} instance.
     * @param <T>      the type of scene the resulting function will join
     * @return a new JoinFunction
     */
    public static @NotNull <T extends Scene> JoinFunction<T> joinFunction(@NotNull Class<T> type,
        @NotNull Function<@NotNull Set<@NotNull PlayerView>, @NotNull Join<T>> function) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(function);

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

    /**
     * Registers a persistent {@link JoinFunction}, associating it with a specific {@link Key}. JoinFunctions can be
     * used to more conveniently submit join requests to this SceneManager
     *
     * @param key      the key associated with the JoinFunction
     * @param function the JoinFunction associated with the key
     * @param <T>      the scene type
     * @throws IllegalArgumentException if the key type is not equal to the function type, or if a function is already
     *                                  bound to the given key
     */
    public <T extends Scene> void registerJoinFunction(@NotNull Key<T> key, @NotNull JoinFunction<T> function) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(function);

        if (!key.type.equals(function.type())) {
            throw new IllegalArgumentException("mismatch between Key type and JoinFunction type");
        }

        if (functionMap.putIfAbsent(key, function) != null) {
            throw new IllegalArgumentException("Key " + key + " already associated with a JoinFunction");
        }
    }

    /**
     * Attempts to fulfill a {@link Join} created by calling the {@link JoinFunction} associated with the given
     * {@link Key} with the set of {@link PlayerView}s participating in the join.
     *
     * @param key     the key used to look up a JoinFunction
     * @param players the set of joining players
     * @param <T>     the type of scene to join
     * @return the result of calling {@link SceneManager#joinScene(Join)} on the Join instanced returned by a registered
     * JoinFunction
     * @throws IllegalArgumentException if there is no JoinFunction mapped to the given key
     * @see SceneManager#joinScene(Join)
     */
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
     * by the login hook function. If a scene is successfully found, the LoginJoin instance is saved for use in the
     * following two stages.
     * <p>
     * At some point later, a {@link PlayerTablistShowEvent} is triggered and
     * {@link LoginJoin#updateLoginTablist(InstanceScene, PlayerTablistShowEvent)} is called to determine which players
     * should see the joining player in the tablist. Generally speaking, this is only players in the scene that the
     * player is joining, but different LoginJoin implementations will have different rules.
     * <p>
     * Finally, {@link PlayerSpawnEvent} is called to initially spawn in the player. At this point, the saved
     * LoginJoin instance is removed and the {@link LoginJoin#postSpawn(InstanceScene)} method is called, which will
     * fully add the player to the scene, as they now have their instance properly set.
     *
     * @param requestMapper the function used to create Join objects for players in response to login events
     */
    public void setLoginHook(@Nullable Function<? super @NotNull Player, ? extends @NotNull LoginJoin<? extends InstanceScene>> requestMapper) {
        this.requestMapper = requestMapper;
    }

    /**
     * Sets the default fallback for players. This is a function that, given a set of players, will perform some
     * (synchronous or asynchronous) computation that may move them to a new scene, kick them from the server, or do
     * nothing. In most cases, this function is expected to attempt to fulfill a specific <i>join</i> for the players.
     * <p>
     * The default value of this function is {@code null}. When null, nothing will be done to move players out of scenes
     * that are shutting down. In this case, players may be entirely kicked from the server by the scene when
     * {@link Scene#shutdown()} is called, but this behavior is not required.
     * <p>
     * The default fallback is only used when {@link SceneManager#removeScene(Scene)} is called. If
     * {@link SceneManager#removeScene(Scene, Function)} is used instead, the function passed to that method will be
     * used in favor of the default fallback.
     *
     * @param fallback the default fallback, or {@code null} to use no fallback (default value)
     */
    public void setDefaultFallback(@Nullable Function<? super @NotNull Set<@NotNull PlayerView>, ? extends @NotNull CompletableFuture<?>> fallback) {
        this.globalFallback = fallback;
    }

    /**
     * Removes a scene, shutting it down and scheduling it for removal from internal data structures. If the scene is
     * not managed by this manager, or is of an unrecognized type, this method will do nothing.
     * <p>
     * Note that this method will <i>leave</i> any players from the scene when it has shut down, but these players will
     * not be sent anywhere. That is the responsibility of {@code shutdownCallback}, a
     * {@link CompletableFuture}-returning function. When its future is complete (exceptionally or otherwise), the scene
     * will be fully shut down by calling {@link Scene#shutdown()}.
     *
     * @param scene            the scene to remove
     * @param shutdownCallback the callback to run with the set of all players that were previously in {@code scene}
     */
    public void removeScene(@NotNull Scene scene,
        @Nullable Function<? super @NotNull Set<@NotNull PlayerView>, ? extends @NotNull CompletableFuture<?>> shutdownCallback) {
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
        Set<Player> leftPlayers;
        boolean playersLocked = false;

        try {
            try {
                scene.preShutdown();

                players = Set.copyOf(scene.playersView());
                if (players.isEmpty()) {
                    //we can shut down immediately if we have no players
                    scene.shutdown();
                    EventDispatcher.call(new SceneShutdownEvent(scene));
                    return;
                }

                playersLocked = lockPlayers(players, true);

                leftPlayers = unwrapMany(scene.leave(players), HashSet::new);
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

        boolean hasLeftPlayers = !leftPlayers.isEmpty();

        Set<Player> finalLeftPlayers = leftPlayers;
        if (!hasLeftPlayers || shutdownCallback == null) {
            scene.getAcquirable().sync(self -> {
                if (hasLeftPlayers) {
                    self.postLeave(finalLeftPlayers);
                }

                self.shutdown();
                EventDispatcher.call(new SceneShutdownEvent(self));
            });

            return;
        }

        shutdownCallback.apply(players).whenComplete((ignored1, ignored2) -> scene.getAcquirable().sync(self -> {
            self.postLeave(finalLeftPlayers);
            self.shutdown();
            EventDispatcher.call(new SceneShutdownEvent(self));
        }));
    }

    /**
     * Removes a scene, using the <i>default fallback</i> to move existing players out of the scene (see
     * {@link SceneManager#setDefaultFallback(Function)} for more details). Otherwise, behaves exactly as
     * {@link SceneManager#removeScene(Scene, Function)}.
     *
     * @param scene the scene to remove
     */
    public void removeScene(@NotNull Scene scene) {
        removeScene(scene, globalFallback);
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
     * @return a set of scenes of the specified type; empty if such a type has not been registered or if there are not
     * any scenes of that type
     */
    @SuppressWarnings("unchecked")
    public <T extends Scene> @UnmodifiableView @NotNull Set<T> typed(@NotNull Class<T> sceneType) {
        SceneEntry entry = mappedScenes.get(sceneType);
        if (entry == null) {
            return Set.of();
        }

        return (Set<T>) Collections.unmodifiableSet(entry.scenes);
    }

    /**
     * Iterates every scene managed by this manager, calling {@link Consumer#accept(Object)} with every instance. None
     * of the scenes will be acquired by the calling thread.
     *
     * @param consumer the consumer which accepts scene objects
     */
    public void forEachScene(@NotNull Consumer<? super @NotNull Scene> consumer) {
        for (Class<? extends Scene> type : mappedScenes.keySet()) {
            for (Scene scene : mappedScenes.get(type).scenes) {
                consumer.accept(scene);
            }
        }
    }

    /**
     * Retrieves the number of scenes that exactly match a specified type. If the type has not been registered,
     * {@code -1} is returned.
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
     * will immediately fail (they will return a CompletableFuture containing a {@link JoinResult} indicating the
     * failure, with a status of {@link JoinStatus#ALREADY_JOINING}).
     *
     * @param join the Join to fulfill
     * @param <T>  the type of scene
     * @return a {@link CompletableFuture} containing a {@link JoinResult} indicating the success or failure of the
     * join, as well as the scene that was joined (if successful)
     */
    public <T extends Scene> @NotNull CompletableFuture<JoinResult<T>> joinScene(@NotNull Join<T> join) {
        Set<PlayerView> players = join.playerViews();
        if (players.isEmpty()) {
            EventDispatcher.call(new SceneJoinEvent(JoinResult.emptyPlayers(), players));
            return CompletableFuture.completedFuture(JoinResult.emptyPlayers());
        }

        List<Map.Entry<Class<? extends Scene>, SceneEntry>> targetEntries = new ArrayList<>();
        for (Map.Entry<Class<? extends Scene>, SceneEntry> entry : mappedScenes.entrySet()) {
            if (join.targetType().isAssignableFrom(entry.getKey())) {
                targetEntries.add(entry);
            }
        }

        if (targetEntries.isEmpty()) {
            EventDispatcher.call(new SceneJoinEvent(JoinResult.unrecognizedType(), players));
            return CompletableFuture.completedFuture(JoinResult.unrecognizedType());
        }

        CompletableFuture<JoinResult<T>> result = CompletableFuture.supplyAsync(() -> {
            if (!lockPlayers(players, false)) {
                return JoinResult.alreadyJoining();
            }

            try {
                for (Map.Entry<Class<? extends Scene>, SceneEntry> entry : targetEntries) {
                    SceneEntry sceneEntry = entry.getValue();

                    T joinedScene = tryJoinScenes(sceneEntry.scenes, join);
                    if (joinedScene != null) {
                        return JoinResult.joined(joinedScene);
                    }

                    synchronized (sceneEntry.creationLock) {
                        joinedScene = tryJoinScenes(sceneEntry.scenes, join);
                        if (joinedScene != null) {
                            return JoinResult.joined(joinedScene);
                        }

                        if (!join.canCreateNewScene(this)) {
                            continue;
                        }

                        T newScene = createAndJoinNewScene(join, entry.getKey());

                        sceneEntry.scenes.add(newScene);
                        threadDispatcher.createPartition(newScene);

                        EventDispatcher.call(new SceneCreationEvent(newScene));
                        return JoinResult.joined(newScene);
                    }
                }

                return JoinResult.cannotProvision();
            } finally {
                unlockPlayers(players);
            }
        }, executor);

        return result.whenComplete((joinResult, error) -> {
            EventDispatcher.call(new SceneJoinEvent((joinResult == null || error != null) ?
                JoinResult.INTERNAL_ERROR : joinResult, players));

            if (error != null) {
                LOGGER.warn("Error while joining player(s) {}: {}",
                    Arrays.deepToString(players.toArray()), error);
            }
        });
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

    private boolean lockPlayers(Collection<? extends PlayerView> players, boolean force) {
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

    private void unlockPlayers(Iterable<? extends PlayerView> players) {
        for (PlayerView playerView : players) {
            ((PlayerViewImpl) playerView).joinLock().unlock();
        }
    }

    /**
     * Gets an Optional that may contain the current scene the player is in, which will be empty if the player does not
     * belong to a scene.
     *
     * @param playerView the player to check
     * @return an Optional containing the current scene
     */
    public @NotNull Optional<Scene> currentScene(@NotNull PlayerView playerView) {
        return ((PlayerViewImpl) playerView).currentScene();
    }

    /**
     * Equivalent to {@link SceneManager#currentScene(PlayerView)}, but accepts a {@link Player} rather than a
     * {@link PlayerView}.
     *
     * @param player the player to check
     * @return an Optional containing the current scene
     */
    public @NotNull Optional<Scene> currentScene(@NotNull Player player) {
        return currentScene(PlayerViewProvider.Global.instance().fromPlayer(player));
    }

    /**
     * Works the same as {@link SceneManager#currentScene(PlayerView)}, but will additionally be empty if the player's
     * current scene is not the same as the provided type. Otherwise, the returned optional will contain a scene that
     * has been cast to the type.
     *
     * @param playerView the player to check
     * @param type       the type of scene
     * @param <T>        the type of scene to check for
     * @return an Optional containing the current scene, cast to the target type
     */
    public @NotNull <T extends Scene> Optional<T> currentScene(@NotNull PlayerView playerView, @NotNull Class<T> type) {
        return ((PlayerViewImpl) playerView).currentScene().filter(scene -> scene.getClass().equals(type)).map(type::cast);
    }

    /**
     * Equivalent to {@link SceneManager#currentScene(PlayerView, Class)}, but accepts a {@link Player} rather than a
     * {@link PlayerView}.
     *
     * @param player the player to check
     * @param type   the type of scene
     * @param <T>    the type of scene to check for
     * @return an Optional containing the current scene, cast to the target type
     */
    public @NotNull <T extends Scene> Optional<T> currentScene(@NotNull Player player, @NotNull Class<T> type) {
        return currentScene(PlayerViewProvider.Global.instance().fromPlayer(player), type);
    }

    /**
     * Tests if the player is in the given scene. Should generally be much faster than running
     * {@link Scene#hasPlayer(Player)} as it does not have to run {@link Set#contains(Object)} or acquire any locks.
     *
     * @param player the player to test
     * @param scene  the scene to test
     * @return true if the player is in the given scene; false otherwise
     */
    public boolean inScene(@NotNull PlayerView player, @NotNull Scene scene) {
        PlayerViewImpl view = (PlayerViewImpl) player;
        return view.currentSceneNullable() == scene;
    }

    /**
     * Equivalent to {@link SceneManager#inScene(PlayerView, Scene)}, but accepts a {@link Player} rather than a
     * {@link PlayerView}.
     *
     * @param player the player to test
     * @param scene  the scene to test
     * @return true if the player is in the given scene; false otherwise
     */
    public boolean inScene(@NotNull Player player, @NotNull Scene scene) {
        return inScene(PlayerViewProvider.Global.instance().fromPlayer(player), scene);
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
     * Equivalent to {@link SceneManager#synchronizeWithCurrentScene(PlayerView, BiConsumer)}, but accepts a
     * {@link Player} rather than a {@link PlayerView}.
     *
     * @param player   the player to synchronize on
     * @param consumer the consumer to run with both the player and current scene passed as arguments
     */
    public void synchronizeWithCurrentScene(@NotNull Player player,
        @NotNull BiConsumer<? super @NotNull PlayerView, ? super @NotNull Scene> consumer) {
        synchronizeWithCurrentScene(PlayerViewProvider.Global.instance().fromPlayer(player), consumer);
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

    /*
    Using the provided Join, creates a new scene and joins it. The created Scene's type must equal 'type'; if not, an
    IllegalStateException will be thrown after immediately shutting down the newly-created scene.

    The general sequence of events when running this function is as follows:

    * New scene is created
    * The players participating in the Join leave their old scenes
    * The new scene is joined
    * The viewable rules of the participating players are reset
    * The new scene is post-joined
    * The old scenes are post-left
     */
    private <T extends Scene> T createAndJoinNewScene(Join<T> join, Class<? extends Scene> type) {
        T scene = join.createNewScene(this);
        if (!scene.getClass().equals(type)) {
            scene.preShutdown();
            scene.shutdown();
            throw new IllegalStateException("Created scene type is not the same as the entry type");
        }

        Iterable<Runnable> actions = leaveOldScenes(join.playerViews(), scene);

        //not necessary to acquire, scene was just created but is not ticking yet
        join.join(scene);
        resetViewableRules(join.playerViews());
        join.postJoin(scene);

        for (Runnable runnable : actions) {
            runnable.run();
        }

        return scene;
    }

    /*
    Works similarly to createAndJoinNewScene, but attempts to join 'scene' instead of creating a new scene. 'scene' must
    be assignable to the target type of 'join'.
     */
    private <T extends Scene> T tryJoinScene(Scene scene, Join<T> join) {
        T castScene = join.targetType().cast(scene);

        Acquired<? extends Scene> acquired = castScene.getAcquirable().lock();
        try {
            if (!castScene.joinable() || !join.matches(castScene)) {
                return null;
            }

            Iterable<Runnable> actions = leaveOldScenes(join.playerViews(), castScene);
            join.join(castScene);
            resetViewableRules(join.playerViews());
            join.postJoin(castScene);

            for (Runnable runnable : actions) {
                runnable.run();
            }
        } finally {
            acquired.unlock();
        }

        return castScene;
    }

    private void resetViewableRules(Iterable<PlayerView> playerViews) {
        for (PlayerView playerView : playerViews) {
            playerView.getPlayer().ifPresent(player -> {
                player.updateViewableRule(null);
                player.updateViewerRule(null);
            });
        }
    }

    private Iterable<Runnable> leaveOldScenes(Set<? extends PlayerView> players, Scene newScene) {
        if (players.isEmpty()) {
            return List.of();
        }

        if (players.size() == 1) {
            PlayerViewImpl onlyPlayer = (PlayerViewImpl) players.iterator().next();

            Optional<Scene> oldSceneOptional = onlyPlayer.currentScene();
            if (oldSceneOptional.isEmpty()) {
                onlyPlayer.updateCurrentScene(newScene);
                return List.of();
            }

            Scene oldScene = oldSceneOptional.get();
            if (oldScene == newScene) {
                return List.of();
            }

            List<Runnable> leaveActions = new ArrayList<>(1);
            processLeavingPlayer(oldScene, players, leaveActions);
            onlyPlayer.updateCurrentScene(newScene);
            return leaveActions;
        }

        List<Runnable> leaveActions = new ArrayList<>(players.size());
        Map<Scene, Set<PlayerViewImpl>> groupedScenes = new HashMap<>(4);
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
            processLeavingPlayer(entry.getKey(), entry.getValue(), leaveActions);

            for (PlayerViewImpl view : value) {
                view.updateCurrentScene(newScene);
            }
        }

        return leaveActions;
    }

    private static void processLeavingPlayer(Scene scene, Set<? extends PlayerView> views, List<Runnable> leaveActions) {
        scene.getAcquirable().sync(self -> {
            Set<Player> left = PlayerView.getMany(self.leave(views), HashSet::new);
            if (!left.isEmpty()) {
                for (Player player : left) {
                    player.clearTags();
                }

                leaveActions.add(() -> self.getAcquirable().sync(self2 -> self2.postLeave(left)));
            }
        });
    }

    private void tick(long time) {
        threadDispatcher.updateAndAwait(time);
        threadDispatcher.refreshThreads(System.currentTimeMillis() - time);
    }
}