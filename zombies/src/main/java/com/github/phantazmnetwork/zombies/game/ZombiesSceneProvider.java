package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.ClientBlockHandlerSource;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.core.entity.fakeplayer.MinimalFakePlayer;
import com.github.phantazmnetwork.core.game.scene.SceneProviderAbstract;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.core.instance.InstanceLoader;
import com.github.phantazmnetwork.core.inventory.BasicInventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.BasicInventoryProfile;
import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.core.time.*;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.audience.ChatComponentSender;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.game.coin.BasicPlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.component.BasicTransactionComponentCreator;
import com.github.phantazmnetwork.zombies.game.corpse.Corpse;
import com.github.phantazmnetwork.zombies.game.kill.BasicPlayerKills;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.listener.*;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.player.BasicZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayerMeta;
import com.github.phantazmnetwork.zombies.game.player.state.*;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.GroupSidebarUpdaterActivable;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.SidebarUpdater;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.*;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section.CollectionSidebarSection;
import com.github.phantazmnetwork.zombies.game.stage.*;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZombiesSceneProvider extends SceneProviderAbstract<ZombiesScene, ZombiesJoinRequest> {

    private record SceneContext(@NotNull EventNode<?> node) {

        public SceneContext {
            Objects.requireNonNull(node, "node");
        }

    }

    private final IdentityHashMap<ZombiesScene, SceneContext> contexts;

    private final MapInfo mapInfo;

    private final InstanceManager instanceManager;

    private final InstanceLoader instanceLoader;

    private final SceneFallback sceneFallback;

    private final EventNode<Event> eventNode;

    private final MobStore mobStore;

    private final MobSpawner mobSpawner;

    private final Map<Key, MobModel> mobModels;

    private final ClientBlockHandlerSource clientBlockHandlerSource;
    private final ContextManager contextManager;
    private final KeyParser keyParser;

    public ZombiesSceneProvider(int maximumScenes, @NotNull MapInfo mapInfo, @NotNull InstanceManager instanceManager,
            @NotNull InstanceLoader instanceLoader, @NotNull SceneFallback sceneFallback,
            @NotNull EventNode<Event> eventNode, @NotNull MobStore mobStore, @NotNull MobSpawner mobSpawner,
            @NotNull Map<Key, MobModel> mobModels, @NotNull ClientBlockHandlerSource clientBlockHandlerSource,
            @NotNull ContextManager contextManager, @NotNull KeyParser keyParser) {
        super(maximumScenes);
        this.contexts = new IdentityHashMap<>(maximumScenes);
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.sceneFallback = Objects.requireNonNull(sceneFallback, "sceneFallback");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobModels = Objects.requireNonNull(mobModels, "mobModels");
        this.clientBlockHandlerSource = Objects.requireNonNull(clientBlockHandlerSource, "clientBlockHandlerSource");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    protected @NotNull Optional<ZombiesScene> chooseScene(@NotNull ZombiesJoinRequest request) {
        for (ZombiesScene scene : getScenes()) {
            if (scene.isComplete() || !scene.isJoinable() || scene.isShutdown()) {
                continue;
            }

            Stage stage = scene.getCurrentStage();
            if (stage == null || stage.hasPermanentPlayers()) {
                continue;
            }

            int maxPlayers = scene.getMapSettingsInfo().maxPlayers();
            int currentPlayerCount = scene.getZombiesPlayers().size();

            if (currentPlayerCount + request.getPlayers().size() <= maxPlayers) {
                return Optional.of(scene);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected @NotNull ZombiesScene createScene(@NotNull ZombiesJoinRequest request) {
        Instance instance =
                instanceLoader.loadInstance(instanceManager, List.of(mapInfo.settings().id().value())); // TODO:verify

        Phaser phaser = new Phaser(1);
        Point spawn = VecUtils.toPoint(mapInfo.settings().spawn().add(mapInfo.settings().origin()));
        ChunkUtils.forChunksInRange(spawn, MinecraftServer.getChunkViewDistance(), (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });
        phaser.arriveAndAwaitAdvance();

        Map<UUID, ZombiesPlayer> zombiesPlayers = new HashMap<>(mapInfo.settings().maxPlayers());
        Random random = new Random();
        ClientBlockHandler blockHandler = clientBlockHandlerSource.forInstance(instance);
        SpawnDistributor spawnDistributor = new BasicSpawnDistributor(mobModels::get, random, zombiesPlayers.values());
        ZombiesMap map = new ZombiesMap(mapInfo, contextManager, instance, mobSpawner, blockHandler, spawnDistributor,
                keyParser);
        Stage idle = new IdleStage(Collections.emptyList(), zombiesPlayers.values());
        Wrapper<Long> countdownTicksRemaining = Wrapper.of(200L);
        Stage countdown = new CountdownStage(Collections.singleton(
                new GroupSidebarUpdaterActivable(zombiesPlayers.values(), mapInfo.settings().maxPlayers()) {
                    @Override
                    protected @NotNull SidebarUpdater createUpdater(@NotNull ZombiesPlayer zombiesPlayer) {
                        return new SidebarUpdater(zombiesPlayer.getSidebar(), Collections.singleton(
                                new CollectionSidebarSection(List.of(new ConstantSidebarLineUpdater(
                                                Component.textOfChildren(Component.text("Map: "),
                                                        mapInfo.settings().displayName())),
                                        new JoinedPlayersSidebarLineUpdater(zombiesPlayers.values(),
                                                mapInfo.settings().maxPlayers()),
                                        new ConstantSidebarLineUpdater(Component.empty()),
                                        new TicksLineUpdater(countdownTicksRemaining, new MappedTickFormatter(
                                                new DurationTickFormatter(NamedTextColor.GREEN, true)) {
                                            @Override
                                            protected @NotNull Component map(@NotNull Component ticksComponent) {
                                                return Component.textOfChildren(Component.text("Starting in "),
                                                        ticksComponent);
                                            }
                                        })))));
                    }
                }), zombiesPlayers.values(), countdownTicksRemaining, countdownTicksRemaining.get());
        Wrapper<Long> ticksSinceStart = Wrapper.of(0L);
        Stage inGame = new InGameStage(Collections.singleton(
                new GroupSidebarUpdaterActivable(zombiesPlayers.values(), mapInfo.settings().maxPlayers()) {
                    @Override
                    protected @NotNull SidebarUpdater createUpdater(@NotNull ZombiesPlayer zombiesPlayer) {
                        List<SidebarLineUpdater> lineUpdaters = new ArrayList<>();
                        lineUpdaters.add(new RoundSidebarLineUpdater(map));
                        lineUpdaters.add(new RemainingZombiesSidebarLineUpdater(map::currentRound));
                        lineUpdaters.add(new ConstantSidebarLineUpdater(Component.empty()));
                        CompletableFuture<Component> displayNameFuture = zombiesPlayer.getPlayerView().getDisplayName();
                        lineUpdaters.add(new ConditionalSidebarLineUpdater(List.of(Pair.of(
                                        () -> zombiesPlayer.getStateSwitcher().getState().key()
                                                .equals(ZombiesPlayerStateKeys.ALIVE),
                                        new CoinsSidebarLineUpdater(displayNameFuture, zombiesPlayer.getCoins())),
                                Pair.of(() -> true, new PlayerStateSidebarLineUpdater(displayNameFuture,
                                        zombiesPlayer.getStateSwitcher())))));
                        for (ZombiesPlayer otherZombiesPlayer : zombiesPlayers.values()) {
                            if (otherZombiesPlayer != zombiesPlayer) {
                                CompletableFuture<Component> otherDisplayNameFuture =
                                        otherZombiesPlayer.getPlayerView().getDisplayName();
                                lineUpdaters.add(new ConditionalSidebarLineUpdater(List.of(Pair.of(
                                        () -> otherZombiesPlayer.getStateSwitcher().getState().key()
                                                .equals(ZombiesPlayerStateKeys.ALIVE),
                                        new CoinsSidebarLineUpdater(otherDisplayNameFuture,
                                                otherZombiesPlayer.getCoins())), Pair.of(() -> true,
                                        new PlayerStateSidebarLineUpdater(otherDisplayNameFuture,
                                                otherZombiesPlayer.getStateSwitcher())))));
                            }
                        }
                        lineUpdaters.add(new ConstantSidebarLineUpdater(Component.empty()));
                        lineUpdaters.add(new ZombieKillsSidebarLineUpdater(zombiesPlayer.getKills()));
                        lineUpdaters.add(new TicksLineUpdater(ticksSinceStart, new MappedTickFormatter(
                                new AnalogTickFormatter(NamedTextColor.GREEN, NamedTextColor.GREEN, false)) {
                            @Override
                            protected @NotNull Component map(@NotNull Component ticksComponent) {
                                return Component.textOfChildren(Component.text("Time: "), ticksComponent);
                            }
                        }));
                        return new SidebarUpdater(zombiesPlayer.getSidebar(),
                                Collections.singleton(new CollectionSidebarSection(lineUpdaters)));
                    }
                }), map, ticksSinceStart);
        Stage endGame = new EndGameStage(Collections.singleton(
                new GroupSidebarUpdaterActivable(zombiesPlayers.values(), mapInfo.settings().maxPlayers()) {
                    @Override
                    protected @NotNull SidebarUpdater createUpdater(@NotNull ZombiesPlayer zombiesPlayer) {
                        List<SidebarLineUpdater> lineUpdaters = new ArrayList<>();
                        lineUpdaters.add(new RoundSidebarLineUpdater(map));
                        lineUpdaters.add(new RemainingZombiesSidebarLineUpdater(map::currentRound));
                        lineUpdaters.add(new ConstantSidebarLineUpdater(Component.empty()));
                        CompletableFuture<Component> displayNameFuture = zombiesPlayer.getPlayerView().getDisplayName();
                        lineUpdaters.add(new ConditionalSidebarLineUpdater(List.of(Pair.of(
                                        () -> zombiesPlayer.getStateSwitcher().getState().key()
                                                .equals(ZombiesPlayerStateKeys.ALIVE),
                                        new CoinsSidebarLineUpdater(displayNameFuture, zombiesPlayer.getCoins())),
                                Pair.of(() -> true, new PlayerStateSidebarLineUpdater(displayNameFuture,
                                        zombiesPlayer.getStateSwitcher())))));
                        for (ZombiesPlayer otherZombiesPlayer : zombiesPlayers.values()) {
                            if (otherZombiesPlayer != zombiesPlayer) {
                                CompletableFuture<Component> otherDisplayNameFuture =
                                        otherZombiesPlayer.getPlayerView().getDisplayName();
                                lineUpdaters.add(new ConditionalSidebarLineUpdater(List.of(Pair.of(
                                        () -> otherZombiesPlayer.getStateSwitcher().getState().key()
                                                .equals(ZombiesPlayerStateKeys.ALIVE),
                                        new CoinsSidebarLineUpdater(otherDisplayNameFuture,
                                                otherZombiesPlayer.getCoins())), Pair.of(() -> true,
                                        new PlayerStateSidebarLineUpdater(otherDisplayNameFuture,
                                                otherZombiesPlayer.getStateSwitcher())))));
                            }
                        }
                        lineUpdaters.add(new ConstantSidebarLineUpdater(Component.empty()));
                        lineUpdaters.add(new ZombieKillsSidebarLineUpdater(zombiesPlayer.getKills()));
                        lineUpdaters.add(new TicksLineUpdater(ticksSinceStart, new MappedTickFormatter(
                                new AnalogTickFormatter(NamedTextColor.GREEN, NamedTextColor.GREEN, false)) {
                            @Override
                            protected @NotNull Component map(@NotNull Component ticksComponent) {
                                return Component.textOfChildren(Component.text("Time: "), ticksComponent);
                            }
                        }));
                        return new SidebarUpdater(zombiesPlayer.getSidebar(),
                                Collections.singleton(new CollectionSidebarSection(lineUpdaters)));
                    }
                }), zombiesPlayers.values(), 100L);
        StageTransition stageTransition = new StageTransition(List.of(idle, countdown, inGame, endGame));

        EventNode<Event> sceneNode = EventNode.all(instance.getUniqueId().toString());
        sceneNode.addListener(EntityDeathEvent.class,
                new PhantazmMobDeathListener(instance, mobStore, map::currentRound));
        sceneNode.addListener(EntityDamageEvent.class, new PlayerDamageMobListener(instance, mobStore, zombiesPlayers));
        sceneNode.addListener(EntityDamageEvent.class, new PlayerDeathEventListener(instance, zombiesPlayers) {

            @Override
            protected void onPlayerDeath(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
                // todo: set state
            }
        });
        PlayerRightClickListener rightClickListener = new PlayerRightClickListener();
        sceneNode.addListener(PlayerBlockInteractEvent.class,
                new PlayerInteractBlockListener(instance, zombiesPlayers, rightClickListener));
        sceneNode.addListener(PlayerEntityInteractEvent.class,
                new PlayerInteractEntityListener(instance, zombiesPlayers, rightClickListener));
        sceneNode.addListener(PlayerUseItemEvent.class,
                new PlayerUseItemListener(instance, zombiesPlayers, rightClickListener));
        sceneNode.addListener(PlayerUseItemOnBlockEvent.class,
                new PlayerUseItemOnBlockListener(instance, zombiesPlayers, rightClickListener));
        eventNode.addChild(sceneNode);
        Function<PlayerView, ZombiesPlayer> playerCreator = playerView -> {
            ZombiesPlayerMeta meta = new ZombiesPlayerMeta();
            PlayerCoins coins = new BasicPlayerCoins(playerView::getPlayer, new ChatComponentSender(),
                    new BasicTransactionComponentCreator(), 0);
            PlayerKills kills = new BasicPlayerKills();
            InventoryAccessRegistry profileSwitcher = new BasicInventoryAccessRegistry();
            Key profileKey = Key.key(Namespaces.PHANTAZM, "inventory.profile.default");
            InventoryAccess access = new InventoryAccess(new BasicInventoryProfile(9), Collections.emptyMap());
            profileSwitcher.registerAccess(profileKey, access);
            profileSwitcher.switchAccess(profileKey);
            EquipmentHandler equipmentHandler = new EquipmentHandler(access);
            Sidebar sidebar = new Sidebar(Component.text("ZOMBIES", NamedTextColor.RED));
            Supplier<ZombiesPlayerState> defaultStateSupplier =
                    () -> new BasicZombiesPlayerState(Component.text("ALIVE", NamedTextColor.GREEN),
                            ZombiesPlayerStateKeys.ALIVE,
                            Collections.singleton(new BasicZombiesPlayerState.Activable() {
                                @Override
                                public void start(@Nullable Key previousStateKey) {
                                    playerView.getPlayer().ifPresent(player -> {
                                        player.setFlying(false);
                                        player.setAllowFlying(false);
                                        player.setGameMode(GameMode.ADVENTURE);
                                        player.setInvisible(false);
                                        meta.setInGame(true);
                                        meta.setCanRevive(true);
                                        meta.setCanTriggerSLA(true);
                                        if (!sidebar.getViewers().contains(player)) {
                                            sidebar.addViewer(player);
                                        }
                                    });
                                }
                            }));
            Supplier<ZombiesPlayerState> quitStateSupplier =
                    () -> new BasicZombiesPlayerState(Component.text("QUIT", NamedTextColor.RED),
                            ZombiesPlayerStateKeys.QUIT, Collections.singleton(new BasicZombiesPlayerState.Activable() {
                        @Override
                        public void start(@Nullable Key previousStateKey) {
                            Reference<Instance> instanceReference = new WeakReference<>(instance);
                            playerView.getDisplayName().thenAccept(displayName -> {
                                Instance instance = instanceReference.get();
                                if (instance == null) {
                                    return;
                                }

                                instance.sendMessage(
                                        Component.textOfChildren(displayName, Component.text(" has quit")));
                            });
                            playerView.getPlayer().ifPresent(sidebar::removeViewer);
                            meta.setInGame(false);
                            meta.setCanRevive(false);
                            meta.setCanTriggerSLA(false);
                        }

                        @Override
                        public void end(@Nullable Key nextStateKey) {
                            if (nextStateKey != null) {
                                Reference<Instance> instanceReference = new WeakReference<>(instance);
                                playerView.getDisplayName().thenAccept(displayName -> {
                                    Instance instance = instanceReference.get();
                                    if (instance == null) {
                                        return;
                                    }

                                    instance.sendMessage(
                                            Component.textOfChildren(displayName, Component.text(" rejoined")));
                                });
                            }
                        }
                    }));
            BiFunction<Component, Collection<BasicZombiesPlayerState.Activable>, ZombiesPlayerState> deadStateFunction =
                    (killerName, actions) -> {
                        List<BasicZombiesPlayerState.Activable> actionsCopy = new ArrayList<>(actions);
                        actionsCopy.add(new BasicZombiesPlayerState.Activable() {
                            @Override
                            public void start(@Nullable Key previousStateKey) {
                                Reference<Instance> instanceReference = new WeakReference<>(instance);
                                playerView.getPlayer().ifPresent(player -> {
                                    player.setAllowFlying(true);
                                    player.setFlying(true);
                                    player.setGameMode(GameMode.ADVENTURE);
                                    player.setInvisible(true);
                                    meta.setCanRevive(false);
                                    meta.setCanTriggerSLA(false);
                                    if (!sidebar.getViewers().contains(player)) {
                                        sidebar.addViewer(player);
                                    }
                                });
                                if (previousStateKey != ZombiesPlayerStateKeys.QUIT) {
                                    playerView.getDisplayName().thenAccept(displayName -> {
                                        Instance instance = instanceReference.get();
                                        if (instance == null) {
                                            return;
                                        }

                                        if (killerName != null) {
                                            instance.sendMessage(Component.textOfChildren(displayName,
                                                    Component.text(" was killed" + " by"), killerName));
                                        }
                                        else {
                                            instance.sendMessage(Component.textOfChildren(displayName,
                                                    Component.text(" was killed")));
                                        }
                                    });
                                }
                            }
                        });
                        return new BasicZombiesPlayerState(Component.text("DEAD", NamedTextColor.RED),
                                ZombiesPlayerStateKeys.DEAD, actionsCopy);
                    };
            Function<Component, ZombiesPlayerState> knockedStateFunction = killerName -> {
                Point minestomLocation = null;
                Vec3I location = null;
                Component roomName = null;
                Optional<Player> playerOptional = playerView.getPlayer();
                if (playerOptional.isPresent()) {
                    minestomLocation = playerOptional.get().getPosition();
                    location = VecUtils.toBlockInt(minestomLocation);
                    roomName = map.roomAt(location).map(room -> room.getData().displayName()).orElse(null);
                }
                Component finalRoomName = roomName;
                ArrayList<KnockedPlayerState.Activable> knockedActions = new ArrayList<>(2);
                TickFormatter tickFormatter = new PrecisionSecondTickFormatter(NamedTextColor.YELLOW, 1);
                knockedActions.add(new KnockedPlayerState.Activable() {

                    private final CompletableFuture<? extends Component> displayNameFuture =
                            playerView.getDisplayName();

                    @Override
                    public void start(@Nullable Key previousStateKey) {
                        Reference<Instance> instanceReference = new WeakReference<>(instance);
                        playerView.getDisplayName().thenAccept(displayName -> {
                            Instance instance = instanceReference.get();
                            if (instance == null) {
                                return;
                            }

                            if (finalRoomName != null) {
                                if (killerName != null) {
                                    instance.sendMessage(
                                            Component.textOfChildren(displayName, Component.text(" was killed by "),
                                                    killerName, Component.text(" in "), finalRoomName));
                                }
                                else {
                                    instance.sendMessage(
                                            Component.textOfChildren(displayName, Component.text(" was killed in "),
                                                    finalRoomName));
                                }
                            }
                            else if (killerName != null) {
                                instance.sendMessage(
                                        Component.textOfChildren(displayName, Component.text(" was killed by "),
                                                killerName));
                            }
                            else {
                                instance.sendMessage(
                                        Component.textOfChildren(displayName, Component.text(" was killed ")));
                            }
                        });
                        playerView.getPlayer().ifPresent(player -> {
                            player.setFlying(false);
                            player.setAllowFlying(false);
                            player.setGameMode(GameMode.ADVENTURE);
                            player.setInvisible(true);
                            meta.setCanRevive(false);
                            meta.setCanTriggerSLA(false);
                        });
                    }

                    @Override
                    public void deathTick(long time, long ticksUntilDeath) {
                        playerView.getPlayer().ifPresent(player -> {
                            player.sendActionBar(Component.textOfChildren(Component.text("You will die in "),
                                    tickFormatter.format(ticksUntilDeath)));
                        });
                    }

                    @Override
                    public void reviveTick(long time, @NotNull ZombiesPlayer reviver, long ticksUntilRevive) {
                        playerView.getPlayer().ifPresent(player -> {
                            player.sendActionBar(Component.textOfChildren(Component.text("You are being revived by "),
                                    Component.empty()));
                        }); // TODO
                    }

                    @Override
                    public void end(@Nullable Key nextStateKey, @Nullable ZombiesPlayer reviver) {
                        Reference<Instance> instanceReference = new WeakReference<>(instance);
                        if (reviver != null) {
                            playerView.getDisplayName().thenCombine(reviver.getPlayerView().getDisplayName(),
                                    (displayName, reviverDisplayName) -> {
                                        Instance instance = instanceReference.get();
                                        if (instance == null) {
                                            return null;
                                        }
                                        instance.sendMessage(Component.textOfChildren(displayName,
                                                Component.text(" was revived by "), reviverDisplayName));

                                        return null;
                                    });
                            playerView.getPlayer().ifPresent(player -> {
                                player.sendActionBar(Component.empty());
                            });
                        }
                    }
                });
                ArrayList<BasicZombiesPlayerState.Activable> deadActions = new ArrayList<>(1);
                if (location != null && roomName != null) {
                    //TODO
                    Hologram hologram = new InstanceHologram(Vec3D.of(location), 1.0);
                    hologram.setInstance(instance);
                    StringBuilder usernameBuilder = new StringBuilder(16);
                    for (int i = 0; i < 16; i++) {
                        usernameBuilder.append((char)random.nextInt('a', 'z' + 1));
                    }
                    PlayerSkin skin = playerView.getPlayer().map(Player::getSkin).orElse(null);
                    Entity corpseEntity =
                            new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), usernameBuilder.toString(),
                                    skin);
                    corpseEntity.setInstance(instance, minestomLocation);
                    Corpse corpse = new Corpse(hologram, corpseEntity, tickFormatter);
                    knockedActions.add(new KnockedPlayerState.Activable() {
                        @Override
                        public void start(@Nullable Key previousStateKey) {
                            corpse.start();
                        }

                        @Override
                        public void deathTick(long time, long ticksUntilDeath) {
                            corpse.deathTick(time, ticksUntilDeath);
                        }

                        @Override
                        public void reviveTick(long time, @NotNull ZombiesPlayer reviver, long ticksUntilRevive) {
                            corpse.reviveTick(time, reviver, ticksUntilRevive);
                        }

                        @Override
                        public void end(@Nullable Key nextStateKey, @Nullable ZombiesPlayer reviver) {
                            if (nextStateKey != null && nextStateKey.equals(ZombiesPlayerStateKeys.DEAD)) {
                                corpse.disable();
                            }
                            else {
                                corpse.remove();
                            }
                        }
                    });
                    deadActions.add(new BasicZombiesPlayerState.Activable() {
                        @Override
                        public void end(@Nullable Key nextStateKey) {
                            corpse.remove();
                        }
                    });
                }
                knockedActions.trimToSize();
                deadActions.trimToSize();
                return new KnockedPlayerState(() -> deadStateFunction.apply(null, deadActions), // TODO: killer name
                        defaultStateSupplier, () -> {
                    for (ZombiesPlayer otherZombiesPlayer : zombiesPlayers.values()) {
                        ZombiesPlayerMeta otherMeta = otherZombiesPlayer.getMeta();
                        if (playerView.getUUID() != otherZombiesPlayer.getPlayerView().getUUID() &&
                                otherMeta.isCanRevive() && !otherMeta.isReviving() && otherMeta.isCrouching()) {
                            return otherZombiesPlayer;
                        }
                    }

                    return null;
                }, knockedActions, 500L);
            };
            Map<Key, Supplier<ZombiesPlayerState>> stateSuppliers =
                    Map.of(ZombiesPlayerStateKeys.ALIVE, defaultStateSupplier, ZombiesPlayerStateKeys.KNOCKED, () -> {
                        return knockedStateFunction.apply(null);
                    }, ZombiesPlayerStateKeys.DEAD, () -> {
                        Component roomName = playerView.getPlayer().map(player -> {
                            Vec3I location = VecUtils.toBlockInt(player.getPosition());
                            return map.roomAt(location).map(room -> room.getData().displayName()).orElse(null);
                        }).orElse(null);
                        return deadStateFunction.apply(roomName, Collections.emptyList());
                    }, ZombiesPlayerStateKeys.QUIT, quitStateSupplier);
            PlayerStateSwitcher stateSwitcher = new PlayerStateSwitcher(defaultStateSupplier.get());

            EquipmentCreator temporaryCreator = new EquipmentCreator() {
                @Override
                public boolean hasEquipment(@NotNull Key equipmentKey) {
                    return false;
                }

                @Override
                public @NotNull <TEquipment extends Equipment> Optional<TEquipment> createEquipment(
                        @NotNull Key equipmentKey) {
                    return Optional.empty();
                }
            };

            return new BasicZombiesPlayer(playerView, meta, coins, kills, equipmentHandler, temporaryCreator,
                    profileSwitcher, stateSwitcher, stateSuppliers, sidebar);
        };

        ZombiesScene scene =
                new ZombiesScene(zombiesPlayers, instance, sceneFallback, mapInfo.settings(), stageTransition,
                        playerCreator, random);
        contexts.put(scene, new SceneContext(sceneNode));

        return scene;
    }

    @Override
    protected void cleanupScene(@NotNull ZombiesScene scene) {
        SceneContext context = contexts.remove(scene);
        if (context == null) {
            return;
        }

        eventNode.removeChild(context.node());
    }
}
