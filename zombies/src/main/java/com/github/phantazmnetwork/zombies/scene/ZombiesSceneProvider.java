package com.github.phantazmnetwork.zombies.scene;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.ClientBlockHandlerSource;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.core.entity.fakeplayer.MinimalFakePlayer;
import com.github.phantazmnetwork.core.game.scene.SceneProviderAbstract;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.gui.BasicSlotDistributor;
import com.github.phantazmnetwork.core.gui.SlotDistributor;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.core.instance.InstanceLoader;
import com.github.phantazmnetwork.core.inventory.*;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.core.time.DurationTickFormatter;
import com.github.phantazmnetwork.core.time.TickFormatter;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.mob.trigger.MobTrigger;
import com.github.phantazmnetwork.mob.trigger.MobTriggers;
import com.github.phantazmnetwork.zombies.audience.ChatComponentSender;
import com.github.phantazmnetwork.zombies.coin.BasicPlayerCoins;
import com.github.phantazmnetwork.zombies.coin.BasicTransactionModifierSource;
import com.github.phantazmnetwork.zombies.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.coin.component.BasicTransactionComponentCreator;
import com.github.phantazmnetwork.zombies.corpse.Corpse;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.ZombiesGunModule;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.PlayerAudienceProvider;
import com.github.phantazmnetwork.zombies.kill.BasicPlayerKills;
import com.github.phantazmnetwork.zombies.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.listener.*;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.phantazmnetwork.zombies.map.objects.BasicMapObjectBuilder;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.player.BasicZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayerMeta;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayerModule;
import com.github.phantazmnetwork.zombies.player.state.*;
import com.github.phantazmnetwork.zombies.player.state.context.DeadPlayerStateContext;
import com.github.phantazmnetwork.zombies.player.state.context.KnockedPlayerStateContext;
import com.github.phantazmnetwork.zombies.player.state.context.NoContext;
import com.github.phantazmnetwork.zombies.player.state.revive.BasicKnockedStateActivable;
import com.github.phantazmnetwork.zombies.player.state.revive.KnockedPlayerState;
import com.github.phantazmnetwork.zombies.player.state.revive.NearbyReviverFinder;
import com.github.phantazmnetwork.zombies.player.state.revive.ReviveHandler;
import com.github.phantazmnetwork.zombies.powerup.*;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.ElementSidebarUpdaterCreator;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.SidebarModule;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.SidebarUpdater;
import com.github.phantazmnetwork.zombies.spawn.BasicSpawnDistributor;
import com.github.phantazmnetwork.zombies.spawn.SpawnDistributor;
import com.github.phantazmnetwork.zombies.stage.*;
import com.github.phantazmnetwork.zombies.util.ElementUtils;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Phaser;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZombiesSceneProvider extends SceneProviderAbstract<ZombiesScene, ZombiesJoinRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesSceneProvider.class);

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

    private final MobSpawner mobSpawner;

    private final MobStore mobStore;

    private final Map<Key, MobModel> mobModels;

    private final ClientBlockHandlerSource clientBlockHandlerSource;

    private final ContextManager contextManager;

    private final KeyParser keyParser;

    private final Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction;

    private final Team corpseTeam;

    public ZombiesSceneProvider(int maximumScenes, @NotNull MapInfo mapInfo, @NotNull InstanceManager instanceManager,
            @NotNull InstanceLoader instanceLoader, @NotNull SceneFallback sceneFallback,
            @NotNull EventNode<Event> eventNode, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull Map<Key, MobModel> mobModels, @NotNull ClientBlockHandlerSource clientBlockHandlerSource,
            @NotNull ContextManager contextManager, @NotNull KeyParser keyParser,
            @NotNull Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction, @NotNull Team corpseTeam) {
        super(maximumScenes);
        this.contexts = new IdentityHashMap<>(maximumScenes);
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.sceneFallback = Objects.requireNonNull(sceneFallback, "sceneFallback");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.mobModels = Objects.requireNonNull(mobModels, "mobModels");
        this.clientBlockHandlerSource = Objects.requireNonNull(clientBlockHandlerSource, "clientBlockHandlerSource");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.equipmentCreatorFunction = Objects.requireNonNull(equipmentCreatorFunction, "equipmentCreatorFunction");
        this.corpseTeam = Objects.requireNonNull(corpseTeam, "corpseTeam");
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

    @Override
    protected @NotNull ZombiesScene createScene(@NotNull ZombiesJoinRequest request) {
        MapSettingsInfo settings = mapInfo.settings();
        Instance instance = instanceLoader.loadInstance(instanceManager, settings.instancePath());

        Pos spawnPos = VecUtils.toPos(settings.origin().add(settings.spawn()));
        awaitChunkLoad(instance, spawnPos);
        instance.setTime(settings.worldTime());
        instance.setTimeRate(0);

        //LinkedHashMap for better value iteration performance
        Map<UUID, ZombiesPlayer> zombiesPlayers = new LinkedHashMap<>(settings.maxPlayers());

        TransactionModifierSource transactionModifierSource = new BasicTransactionModifierSource();
        Flaggable flaggable = new BasicFlaggable();
        Random random = new Random();
        MapObjects mapObjects =
                createMapObjects(instance, random, zombiesPlayers, flaggable, transactionModifierSource, spawnPos);
        PowerupHandler powerupHandler = createPowerupHandler(mapObjects.mapDependencyProvider(), zombiesPlayers,
                settings.powerupPickupRadius());
        RoundHandler roundHandler = new BasicRoundHandler(mapObjects.rounds());

        ZombiesMap map = new ZombiesMap(transactionModifierSource, flaggable, mapObjects, powerupHandler, roundHandler);

        EventNode<Event> childNode = createEventNode(instance, zombiesPlayers, mapObjects, roundHandler);

        Wrapper<Long> ticksSinceStart = Wrapper.of(0L);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String dateString = StringUtils.center(dateFormatter.format(LocalDate.now()), 16);
        Component date = Component.text(dateString, TextColor.color(7040896));
        SidebarModule sidebarModule =
                new SidebarModule(zombiesPlayers.values(), roundHandler, ticksSinceStart, date, settings.maxPlayers());
        StageTransition stageTransition =
                createStageTransition(instance, settings.introMessages(), random, zombiesPlayers.values(), spawnPos,
                        roundHandler, ticksSinceStart, sidebarModule);

        Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator = playerView -> {
            return createPlayer(zombiesPlayers, settings, instance, playerView, new BasicTransactionModifierSource(),
                    new BasicFlaggable(), childNode, random, mapObjects);
        };

        ZombiesScene scene = new ZombiesScene(map, zombiesPlayers, instance, sceneFallback, settings, stageTransition,
                playerCreator);

        eventNode.addChild(childNode);
        contexts.put(scene, new SceneContext(childNode));
        return scene;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void awaitChunkLoad(Instance instance, Pos spawnPos) {
        Phaser phaser = new Phaser(1);
        ChunkUtils.forChunksInRange(spawnPos, MinecraftServer.getChunkViewDistance(), (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });
        phaser.arriveAndAwaitAdvance();
    }

    private @NotNull MapObjects createMapObjects(@NotNull Instance instance, @NotNull Random random,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull Flaggable flaggable,
            @NotNull TransactionModifierSource transactionModifierSource, @NotNull Pos spawnPos) {
        ClientBlockHandler blockHandler = clientBlockHandlerSource.forInstance(instance);
        SpawnDistributor spawnDistributor = new BasicSpawnDistributor(mobModels::get, random, zombiesPlayers.values());
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);

        return new BasicMapObjectBuilder(contextManager, instance, mobStore, mobSpawner, blockHandler, spawnDistributor,
                BasicRoundHandler::new, flaggable, transactionModifierSource, slotDistributor, zombiesPlayers, spawnPos,
                keyParser).build(mapInfo);
    }

    private @NotNull PowerupHandler createPowerupHandler(@NotNull DependencyProvider mapDependencyProvider,
            @NotNull Map<UUID, ZombiesPlayer> playerMap, double powerupPickupRadiusSquared) {
        List<PowerupInfo> powerupData = mapInfo.powerups();
        Map<Key, PowerupComponents> powerupMap = new HashMap<>(powerupData.size());

        for (PowerupInfo data : powerupData) {
            ConfigList visualData = data.visuals();
            ConfigList actionData = data.actions();
            ConfigNode deactivationPredicateData = data.deactivationPredicate();

            Collection<Supplier<PowerupVisual>> visuals = new ArrayList<>(visualData.size());
            Collection<Supplier<PowerupAction>> actions = new ArrayList<>(actionData.size());

            ElementUtils.createElements(contextManager, visualData, visuals, "powerup visual", mapDependencyProvider,
                    LOGGER);

            ElementUtils.createElements(contextManager, actionData, actionData, "powerup action", mapDependencyProvider,
                    LOGGER);

            Supplier<DeactivationPredicate> deactivationPredicateSupplier =
                    ElementUtils.createElement(contextManager, deactivationPredicateData,
                            "powerup deactivation predicate", mapDependencyProvider, LOGGER);

            if (deactivationPredicateSupplier == null) {
                deactivationPredicateSupplier = () -> ImmediateDeactivationPredicate.INSTANCE;
            }

            powerupMap.put(data.id(), new PowerupComponents(visuals, actions, deactivationPredicateSupplier));
        }

        return new BasicPowerupHandler(powerupMap, playerMap, powerupPickupRadiusSquared);
    }

    private @NotNull ZombiesPlayer createPlayer(@NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull MapSettingsInfo mapSettingsInfo, @NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull TransactionModifierSource transactionModifierSource, @NotNull Flaggable flaggable,
            @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects) {
        ZombiesPlayerMeta meta = new ZombiesPlayerMeta();
        PlayerCoins coins = new BasicPlayerCoins(new PlayerAudienceProvider(playerView), new ChatComponentSender(),
                new BasicTransactionComponentCreator(), 0);
        PlayerKills kills = new BasicPlayerKills();
        InventoryProfile profile = new BasicInventoryProfile(9);
        InventoryAccess inventoryAccess = new InventoryAccess(profile,
                Map.of(Key.key(Namespaces.PHANTAZM, "inventory.group.gun"),
                        new BasicInventoryObjectGroup(profile, IntSet.of(1, 2))));
        InventoryAccessRegistry accessRegistry = new BasicInventoryAccessRegistry();
        Key accessKey = Key.key(Namespaces.PHANTAZM, "inventory.access.default");
        accessRegistry.registerAccess(accessKey, inventoryAccess);
        accessRegistry.switchAccess(accessKey);
        EquipmentHandler equipmentHandler = new EquipmentHandler(accessRegistry);

        ZombiesGunModule gunModule =
                new ZombiesGunModule(playerView, mobSpawner, mobStore, eventNode, random, mapObjects);
        EquipmentCreator equipmentCreator = equipmentCreatorFunction.apply(gunModule);

        Sidebar sidebar = new Sidebar(
                Component.text(StringUtils.center("ZOMBIES", 16), NamedTextColor.YELLOW, TextDecoration.BOLD));

        Function<NoContext, ZombiesPlayerState> aliveStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("ALIVE", NamedTextColor.GREEN),
                    ZombiesPlayerStateKeys.ALIVE.key(),
                    Collections.singleton(new BasicAliveStateActivable(playerView, meta, sidebar)));
        };
        BiFunction<DeadPlayerStateContext, Collection<Activable>, ZombiesPlayerState> deadStateCreator =
                (context, activables) -> {
                    List<Activable> combinedActivables = new ArrayList<>(activables);
                    combinedActivables.add(new BasicDeadStateActivable(context, instance, playerView, meta, sidebar));
                    return new BasicZombiesPlayerState(Component.text("DEAD", NamedTextColor.RED),
                            ZombiesPlayerStateKeys.DEAD.key(), combinedActivables);
                };
        Function<KnockedPlayerStateContext, ZombiesPlayerState> knockedStateCreator = context -> {
            Hologram hologram = new InstanceHologram(VecUtils.toDouble(context.getKnockLocation()), 1.0);
            PlayerSkin skin = playerView.getPlayer().map(Player::getSkin).orElse(null);
            String corpseUsername = UUID.randomUUID().toString().substring(0, 16);
            Entity corpseEntity = new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), corpseUsername, skin);
            corpseTeam.addMember(corpseUsername);
            TickFormatter tickFormatter = new DurationTickFormatter(NamedTextColor.RED, false, false);
            Corpse corpse = new Corpse(hologram, corpseEntity, tickFormatter);

            Supplier<ZombiesPlayerState> deadStateSupplier = () -> {
                DeadPlayerStateContext deathContext = DeadPlayerStateContext.killed(context.getKiller().orElse(null),
                        context.getKnockRoom().orElse(null));
                return deadStateCreator.apply(deathContext, List.of(corpse.asDeathActivable(), new Activable() {
                    @Override
                    public void end() {
                        meta.setCorpse(null);
                    }
                }));
            };
            ReviveHandler reviveHandler =
                    new ReviveHandler(() -> aliveStateCreator.apply(NoContext.INSTANCE), deadStateSupplier,
                            new NearbyReviverFinder(zombiesPlayers, playerView, mapSettingsInfo.reviveRadius()), 500L);
            return new KnockedPlayerState(reviveHandler,
                    List.of(new BasicKnockedStateActivable(context, instance, playerView, reviveHandler, tickFormatter,
                            meta, sidebar), corpse.asKnockActivable(reviveHandler), new Activable() {
                        @Override
                        public void start() {
                            meta.setCorpse(corpse);
                        }
                    }));
        };
        Function<NoContext, ZombiesPlayerState> quitStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("QUIT", NamedTextColor.RED),
                    ZombiesPlayerStateKeys.QUIT.key(),
                    Collections.singleton(new BasicQuitStateActivable(instance, playerView, meta, sidebar)));
        };
        PlayerStateSwitcher stateSwitcher = new PlayerStateSwitcher(aliveStateCreator.apply(NoContext.INSTANCE));
        Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions =
                Map.of(ZombiesPlayerStateKeys.ALIVE, aliveStateCreator, ZombiesPlayerStateKeys.DEAD,
                        (Function<DeadPlayerStateContext, ZombiesPlayerState>)context -> deadStateCreator.apply(context,
                                Collections.emptyList()), ZombiesPlayerStateKeys.KNOCKED, knockedStateCreator,
                        ZombiesPlayerStateKeys.QUIT, quitStateCreator);

        ZombiesPlayerModule module =
                new ZombiesPlayerModule(playerView, meta, coins, kills, equipmentHandler, equipmentCreator,
                        accessRegistry, stateSwitcher, stateFunctions, sidebar, transactionModifierSource, flaggable);
        return new BasicZombiesPlayer(module);
    }

    private @NotNull EventNode<Event> createEventNode(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull MapObjects mapObjects,
            @NotNull RoundHandler roundHandler) {
        EventNode<Event> node = EventNode.all(UUID.randomUUID().toString());
        node.addListener(EntityDeathEvent.class,
                new PhantazmMobDeathListener(instance, mobStore, roundHandler::currentRound));
        node.addListener(EntityDamageEvent.class, new PlayerDamageMobListener(instance, mobStore, zombiesPlayers));
        PlayerRightClickListener rightClickListener = new PlayerRightClickListener();
        node.addListener(PlayerBlockInteractEvent.class,
                new PlayerInteractBlockListener(instance, zombiesPlayers, rightClickListener));
        node.addListener(PlayerEntityInteractEvent.class,
                new PlayerInteractEntityListener(instance, zombiesPlayers, rightClickListener));
        node.addListener(PlayerUseItemEvent.class,
                new PlayerUseItemListener(instance, zombiesPlayers, rightClickListener));
        node.addListener(PlayerUseItemOnBlockEvent.class,
                new PlayerUseItemOnBlockListener(instance, zombiesPlayers, rightClickListener));
        node.addListener(EntityDamageEvent.class, new PlayerDeathEventListener(instance, zombiesPlayers, mapObjects));
        node.addListener(PlayerHandAnimationEvent.class, new PlayerLeftClickListener(instance, zombiesPlayers));
        node.addListener(PlayerChangeHeldSlotEvent.class, new PlayerItemSelectListener(instance, zombiesPlayers));
        node.addListener(ItemDropEvent.class, new PlayerDropItemListener(instance, zombiesPlayers));
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(instance, zombiesPlayers));
        for (MobTrigger<?> trigger : MobTriggers.TRIGGERS) {
            registerTrigger(node, mobStore, trigger);
        }

        return node;
    }

    private static <T extends Event> void registerTrigger(@NotNull EventNode<? super T> node,
            @NotNull MobStore mobStore, @NotNull MobTrigger<T> trigger) {
        node.addListener(trigger.eventClass(),
                event -> mobStore.useTrigger(trigger.entityGetter().apply(event), trigger.key()));
    }

    private @NotNull StageTransition createStageTransition(@NotNull Instance instance,
            @NotNull List<Component> messages, @NotNull Random random,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Pos spawnPos,
            @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull SidebarModule sidebarModule) {
        Stage idle = new IdleStage(zombiesPlayers);

        LongList alertTicks = LongList.of(400L, 200L, 100L, 80L, 60L, 40L, 20L);
        TickFormatter tickFormatter = new DurationTickFormatter(NamedTextColor.YELLOW, true, false);

        Stage countdown =
                new CountdownStage(instance, zombiesPlayers, messages, random, 400L, alertTicks, tickFormatter,
                        newSidebarUpdaterCreator(sidebarModule, "countdown"));

        Stage inGame = new InGameStage(instance, zombiesPlayers, spawnPos, roundHandler, ticksSinceStart,
                mapInfo.settings().defaultEquipment(), newSidebarUpdaterCreator(sidebarModule, "inGame"));
        Stage end = new EndStage(instance, zombiesPlayers, 200L, newSidebarUpdaterCreator(sidebarModule, "end"));
        return new StageTransition(idle, countdown, inGame, end);
    }

    private @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> newSidebarUpdaterCreator(
            @NotNull SidebarModule sidebarModule, @NotNull String scoreboardSubNode) {
        ElementContext context = contextManager.makeContext(mapInfo.scoreboard());
        return new ElementSidebarUpdaterCreator(sidebarModule, context, keyParser, scoreboardSubNode);
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
