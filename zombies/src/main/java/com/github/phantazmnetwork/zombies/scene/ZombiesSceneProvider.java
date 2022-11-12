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
import com.github.phantazmnetwork.zombies.audience.ChatComponentSender;
import com.github.phantazmnetwork.zombies.coin.BasicModifierSource;
import com.github.phantazmnetwork.zombies.coin.BasicPlayerCoins;
import com.github.phantazmnetwork.zombies.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.coin.PlayerCoins;
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
import com.github.phantazmnetwork.zombies.spawn.BasicSpawnDistributor;
import com.github.phantazmnetwork.zombies.spawn.SpawnDistributor;
import com.github.phantazmnetwork.zombies.stage.*;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
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
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

    private final MobSpawner mobSpawner;

    private final MobStore mobStore;

    private final Map<Key, MobModel> mobModels;

    private final ClientBlockHandlerSource clientBlockHandlerSource;
    private final ContextManager contextManager;
    private final KeyParser keyParser;

    private final Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction;

    public ZombiesSceneProvider(int maximumScenes, @NotNull MapInfo mapInfo, @NotNull InstanceManager instanceManager,
            @NotNull InstanceLoader instanceLoader, @NotNull SceneFallback sceneFallback,
            @NotNull EventNode<Event> eventNode, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull Map<Key, MobModel> mobModels, @NotNull ClientBlockHandlerSource clientBlockHandlerSource,
            @NotNull ContextManager contextManager, @NotNull KeyParser keyParser,
            @NotNull Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction) {
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
        Instance instance = instanceLoader.loadInstance(instanceManager, mapInfo.settings().instancePath());

        Phaser phaser = new Phaser(1);
        Point spawn = VecUtils.toPoint(mapInfo.settings().spawn().add(mapInfo.settings().origin()));
        ChunkUtils.forChunksInRange(spawn, MinecraftServer.getChunkViewDistance(), (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });
        phaser.arriveAndAwaitAdvance();

        Map<UUID, ZombiesPlayer> zombiesPlayers = new HashMap<>(mapInfo.settings().maxPlayers());

        ModifierSource modifierSource = new BasicModifierSource();
        Flaggable flaggable = new BasicFlaggable();
        Random random = new Random();
        Pos spawnPos = VecUtils.toPos(mapInfo.settings().origin().add(mapInfo.settings().spawn()));
        MapObjects mapObjects =
                createMapObjects(instance, random, zombiesPlayers, flaggable, modifierSource, spawnPos, mapInfo);
        RoundHandler roundHandler = new BasicRoundHandler(mapObjects.rounds());
        ZombiesMap map = new ZombiesMap(mapInfo, instance, modifierSource, flaggable, mapObjects, roundHandler);

        EventNode<Event> childNode = createEventNode(instance, zombiesPlayers, roundHandler);

        Stage idle = new IdleStage(Collections.emptyList(), zombiesPlayers.values());
        Stage countdown = new CountdownStage(Collections.emptyList(), zombiesPlayers.values(), Wrapper.of(400L), 400L);
        Stage inGame = new InGameStage(List.of(new Activable() {
            @Override
            public void start() {
                for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
                    zombiesPlayer.getModule().getPlayerView().getPlayer().ifPresent(player -> {
                        player.teleport(spawnPos);
                    });
                }
            }
        }), map, Wrapper.of(0L));
        Stage end = new EndGameStage(Collections.emptyList(), zombiesPlayers.values(), 200L);
        StageTransition stageTransition = new StageTransition(List.of(idle, countdown, inGame, end));
        Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator = playerView -> {
            ZombiesPlayerModule module =
                    createPlayerModule(zombiesPlayers, mapInfo.settings(), instance, playerView, modifierSource,
                            childNode, random, mapObjects);
            return new BasicZombiesPlayer(module);
        };

        ZombiesScene scene =
                new ZombiesScene(zombiesPlayers, instance, sceneFallback, mapInfo.settings(), stageTransition,
                        playerCreator, random);

        eventNode.addChild(childNode);
        contexts.put(scene, new SceneContext(childNode));
        return scene;
    }

    private @NotNull MapObjects createMapObjects(@NotNull Instance instance, @NotNull Random random,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull Flaggable flaggable,
            @NotNull ModifierSource modifierSource, @NotNull Pos spawnPos, @NotNull MapInfo mapInfo) {
        ClientBlockHandler blockHandler = clientBlockHandlerSource.forInstance(instance);
        SpawnDistributor spawnDistributor = new BasicSpawnDistributor(mobModels::get, random, zombiesPlayers.values());
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);

        return new BasicMapObjectBuilder(contextManager, instance, mobSpawner, blockHandler, spawnDistributor,
                BasicRoundHandler::new, flaggable, modifierSource, slotDistributor, zombiesPlayers, spawnPos,
                keyParser).build(mapInfo);
    }

    private @NotNull ZombiesPlayerModule createPlayerModule(
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull MapSettingsInfo mapSettingsInfo, @NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull ModifierSource modifierSource, @NotNull EventNode<Event> eventNode, @NotNull Random random,
            @NotNull MapObjects mapObjects) {
        ZombiesPlayerMeta meta = new ZombiesPlayerMeta();
        PlayerCoins coins = new BasicPlayerCoins(new PlayerAudienceProvider(playerView), new ChatComponentSender(),
                new BasicTransactionComponentCreator(), 0);
        PlayerKills kills = new BasicPlayerKills();
        InventoryProfile profile = new BasicInventoryProfile(9);
        InventoryAccess inventoryAccess = new InventoryAccess(profile,
                Map.of(Key.key(Namespaces.PHANTAZM, "inventory.access.gun"),
                        new BasicInventoryObjectGroup(profile, IntSet.of(1, 2))));
        InventoryAccessRegistry accessRegistry = new BasicInventoryAccessRegistry();
        Key accessKey = Key.key(Namespaces.PHANTAZM, "inventory.access.default");
        accessRegistry.registerAccess(accessKey, inventoryAccess);
        accessRegistry.switchAccess(accessKey);
        EquipmentHandler equipmentHandler = new EquipmentHandler(accessRegistry);

        ZombiesGunModule gunModule =
                new ZombiesGunModule(playerView, mobSpawner, mobStore, eventNode, random, mapObjects);
        EquipmentCreator equipmentCreator = equipmentCreatorFunction.apply(gunModule);

        Sidebar sidebar = new Sidebar(Component.text("ZOMBIES", NamedTextColor.RED));

        Function<NoContext, ZombiesPlayerState> aliveStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("ALIVE", NamedTextColor.GREEN),
                    ZombiesPlayerStateKeys.ALIVE.key(),
                    Collections.singleton(new BasicAliveStateActivable(playerView, sidebar)));
        };
        BiFunction<DeadPlayerStateContext, Collection<Activable>, ZombiesPlayerState> deadStateCreator =
                (context, activables) -> {
                    List<Activable> combinedActivables = new ArrayList<>(activables);
                    combinedActivables.add(new BasicDeadStateActivable(context, instance, playerView, sidebar));
                    return new BasicZombiesPlayerState(Component.text("DEAD", NamedTextColor.RED),
                            ZombiesPlayerStateKeys.DEAD.key(), combinedActivables);
                };
        Function<KnockedPlayerStateContext, ZombiesPlayerState> knockedStateCreator = context -> {
            Hologram hologram = new InstanceHologram(VecUtils.toDouble(context.getKnockLocation()), 1.0);
            PlayerSkin skin = playerView.getPlayer().map(Player::getSkin).orElse(null);
            Entity corpseEntity = new MinimalFakePlayer(MinecraftServer.getSchedulerManager(),
                    UUID.randomUUID().toString().substring(0, 16), skin);
            TickFormatter tickFormatter = new DurationTickFormatter(NamedTextColor.RED, false);
            Corpse corpse = new Corpse(hologram, corpseEntity, tickFormatter);

            Supplier<ZombiesPlayerState> deadStateSupplier = () -> {
                DeadPlayerStateContext deathContext = DeadPlayerStateContext.killed(context.getKiller().orElse(null),
                        context.getKnockRoom().orElse(null));
                return deadStateCreator.apply(deathContext, Collections.singleton(corpse.asDeathActivable()));
            };
            ReviveHandler reviveHandler =
                    new ReviveHandler(() -> aliveStateCreator.apply(NoContext.INSTANCE), deadStateSupplier,
                            new NearbyReviverFinder(zombiesPlayers, playerView, mapSettingsInfo.reviveRadius()), 500L);
            return new KnockedPlayerState(reviveHandler,
                    List.of(new BasicKnockedStateActivable(context, instance, zombiesPlayers, eventNode, playerView,
                            reviveHandler, tickFormatter, sidebar), corpse.asKnockActivable(reviveHandler)));
        };
        Function<NoContext, ZombiesPlayerState> quitStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("QUIT", NamedTextColor.RED),
                    ZombiesPlayerStateKeys.QUIT.key(),
                    Collections.singleton(new BasicQuitStateActivable(instance, playerView, sidebar)));
        };
        PlayerStateSwitcher stateSwitcher = new PlayerStateSwitcher(aliveStateCreator.apply(NoContext.INSTANCE));
        Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions =
                Map.of(ZombiesPlayerStateKeys.ALIVE, aliveStateCreator, ZombiesPlayerStateKeys.DEAD,
                        (Function<DeadPlayerStateContext, ZombiesPlayerState>)context -> deadStateCreator.apply(context,
                                Collections.emptyList()), ZombiesPlayerStateKeys.KNOCKED, knockedStateCreator,
                        ZombiesPlayerStateKeys.QUIT, quitStateCreator);

        return new ZombiesPlayerModule(playerView, meta, coins, kills, equipmentHandler, equipmentCreator,
                accessRegistry, stateSwitcher, stateFunctions, sidebar, modifierSource);
    }

    private @NotNull EventNode<Event> createEventNode(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull RoundHandler roundHandler) {
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
        node.addListener(ItemDropEvent.class, new PlayerDropItemListener(instance, zombiesPlayers));
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(instance, zombiesPlayers));

        return node;
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
