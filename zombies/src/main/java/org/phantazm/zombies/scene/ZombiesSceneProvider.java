package org.phantazm.zombies.scene;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.proxima.solid.Solid;
import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ClientBlockHandlerSource;
import org.phantazm.core.VecUtils;
import org.phantazm.core.game.scene.SceneProviderAbstract;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.sound.BasicSongPlayer;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.core.time.DurationTickFormatter;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.trigger.EventTrigger;
import org.phantazm.mob.trigger.EventTriggers;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.EntityDamageByGunEvent;
import org.phantazm.zombies.listener.*;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.handler.*;
import org.phantazm.zombies.map.objects.BasicMapObjectsSource;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.mob.MobSpawnerSource;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.BasicPowerupHandlerSource;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.powerup.PowerupInfo;
import org.phantazm.zombies.sidebar.ElementSidebarUpdaterCreator;
import org.phantazm.zombies.sidebar.SidebarModule;
import org.phantazm.zombies.sidebar.SidebarUpdater;
import org.phantazm.zombies.stage.*;

import java.util.*;
import java.util.concurrent.Phaser;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZombiesSceneProvider extends SceneProviderAbstract<ZombiesScene, ZombiesJoinRequest> {
    private final Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction;
    private final IdentityHashMap<ZombiesScene, SceneContext> contexts;
    private final MapInfo mapInfo;
    private final InstanceManager instanceManager;
    private final InstanceLoader instanceLoader;
    private final SceneFallback sceneFallback;
    private final EventNode<Event> eventNode;
    private final ContextManager contextManager;
    private final KeyParser keyParser;

    private final MapObjects.Source mapObjectSource;
    private final ZombiesPlayer.Source zombiesPlayerSource;
    private final PowerupHandler.Source powerupHandlerSource;
    private final ShopHandler.Source shopHandlerSource;
    private final WindowHandler.Source windowHandlerSource;
    private final DoorHandler.Source doorHandlerSource;

    public ZombiesSceneProvider(int maximumScenes,
            @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction,
            @NotNull MapInfo mapInfo, @NotNull InstanceManager instanceManager, @NotNull InstanceLoader instanceLoader,
            @NotNull SceneFallback sceneFallback, @NotNull EventNode<Event> eventNode,
            @NotNull MobSpawnerSource mobSpawnerSource, @NotNull Map<Key, MobModel> mobModels,
            @NotNull ClientBlockHandlerSource clientBlockHandlerSource, @NotNull ContextManager contextManager,
            @NotNull KeyParser keyParser, @NotNull Map<Key, PowerupInfo> powerups,
            @NotNull ZombiesPlayer.Source zombiesPlayerSource) {
        super(maximumScenes);
        this.instanceSpaceFunction = Objects.requireNonNull(instanceSpaceFunction, "instanceSpaceFunction");
        this.contexts = new IdentityHashMap<>(maximumScenes);
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.sceneFallback = Objects.requireNonNull(sceneFallback, "sceneFallback");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(powerups, "powerups");

        MapSettingsInfo settingsInfo = mapInfo.settings();

        this.mapObjectSource = new BasicMapObjectsSource(mapInfo, contextManager, mobSpawnerSource, mobModels,
                clientBlockHandlerSource, keyParser);
        this.zombiesPlayerSource = Objects.requireNonNull(zombiesPlayerSource, "zombiesPlayerSource");
        this.powerupHandlerSource =
                new BasicPowerupHandlerSource(powerups, contextManager, settingsInfo.powerupPickupRadius());
        this.shopHandlerSource = new BasicShopHandlerSource();
        this.windowHandlerSource =
                new BasicWindowHandlerSource(settingsInfo.windowRepairRadius(), settingsInfo.windowRepairTicks(),
                        settingsInfo.repairCoins());
        this.doorHandlerSource = new BasicDoorHandlerSource();
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

        MobStore mobStore = new MobStore();

        Wrapper<RoundHandler> roundHandlerWrapper = Wrapper.ofNull();
        Wrapper<PowerupHandler> powerupHandlerWrapper = Wrapper.ofNull();
        Wrapper<WindowHandler> windowHandlerWrapper = Wrapper.ofNull();

        SongPlayer songPlayer = new BasicSongPlayer();
        MapObjects mapObjects =
                createMapObjects(instance, zombiesPlayers, roundHandlerWrapper, mobStore, powerupHandlerWrapper,
                        windowHandlerWrapper, songPlayer);

        RoundHandler roundHandler = new BasicRoundHandler(mapObjects.rounds());
        roundHandlerWrapper.set(roundHandler);

        PowerupHandler powerupHandler =
                createPowerupHandler(instance, zombiesPlayers, mapObjects.mapDependencyProvider());
        powerupHandlerWrapper.set(powerupHandler);

        ShopHandler shopHandler = createShopHandler(mapObjects.shopTracker());

        WindowHandler windowHandler = createWindowHandler(mapObjects.windowTracker(), zombiesPlayers.values());
        windowHandlerWrapper.set(windowHandler);

        DoorHandler doorHandler = createDoorHandler(mapObjects.doorTracker(), mapObjects.roomTracker());

        ZombiesMap map =
                new ZombiesMap(mapObjects, songPlayer, powerupHandler, roundHandler, shopHandler, windowHandler,
                        doorHandler, mobStore);

        EventNode<Event> childNode =
                createEventNode(instance, zombiesPlayers, mapObjects, roundHandler, shopHandler, windowHandler,
                        doorHandler, mapObjects.roomTracker(), mapObjects.windowTracker(), powerupHandler, mobStore);

        Wrapper<Long> ticksSinceStart = Wrapper.of(0L);
        SidebarModule sidebarModule =
                new SidebarModule(zombiesPlayers, zombiesPlayers.values(), roundHandler, ticksSinceStart,
                        settings.maxPlayers());
        StageTransition stageTransition =
                createStageTransition(instance, settings.introMessages(), mapObjects.module().random(),
                        zombiesPlayers.values(), spawnPos, roundHandler, ticksSinceStart, sidebarModule, shopHandler);

        Wrapper<ZombiesScene> sceneWrapper = Wrapper.ofNull();
        Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator = playerView -> {
            playerView.getPlayer().ifPresent(player -> {
                player.getAttribute(Attributes.HEAL_TICKS).setBaseValue(settings.healTicks());
                player.getAttribute(Attributes.REVIVE_TICKS).setBaseValue(settings.baseReviveTicks());
            });

            return zombiesPlayerSource.createPlayer(sceneWrapper.get(), zombiesPlayers, settings, instance, playerView,
                    mapObjects.module().modifierSource(), new BasicFlaggable(), childNode, mapObjects.module().random(),
                    mapObjects, mobStore, mapObjects.mobSpawner());
        };

        ZombiesScene scene = new ZombiesScene(map, zombiesPlayers, instance, sceneFallback, settings, stageTransition,
                playerCreator);
        sceneWrapper.set(scene);

        eventNode.addChild(childNode);
        contexts.put(scene, new SceneContext(childNode));

        InstanceSpawner.InstanceSettings instanceSettings = instanceSpaceFunction.apply(instance);
        instanceSettings.spaceHandler().space().setOverrideFunction((x, y, z) -> {
            if (windowHandler.tracker().atPoint(x, y, z).isPresent()) {
                return Solid.EMPTY;
            }

            return null;
        });

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

    private MapObjects createMapObjects(Instance instance, Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            Supplier<? extends RoundHandler> roundHandlerSupplier, MobStore mobStore,
            Wrapper<PowerupHandler> powerupHandler, Wrapper<WindowHandler> windowHandler, SongPlayer songPlayer) {
        return mapObjectSource.make(instance, zombiesPlayers, roundHandlerSupplier, mobStore, powerupHandler,
                windowHandler, songPlayer);
    }

    private PowerupHandler createPowerupHandler(Instance instance, Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            DependencyProvider mapDependencyProvider) {
        return powerupHandlerSource.make(instance, playerMap, mapDependencyProvider);
    }

    private ShopHandler createShopHandler(BoundedTracker<Shop> shopTracker) {
        return shopHandlerSource.make(shopTracker);
    }

    private WindowHandler createWindowHandler(BoundedTracker<Window> windowTracker,
            Collection<? extends ZombiesPlayer> players) {
        return windowHandlerSource.make(windowTracker, players);
    }

    private DoorHandler createDoorHandler(BoundedTracker<Door> doorTracker, BoundedTracker<Room> roomTracker) {
        return doorHandlerSource.make(doorTracker, roomTracker);
    }

    private @NotNull EventNode<Event> createEventNode(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull MapObjects mapObjects,
            @NotNull RoundHandler roundHandler, @NotNull ShopHandler shopHandler, @NotNull WindowHandler windowHandler,
            @NotNull DoorHandler doorHandler, @NotNull BoundedTracker<Room> roomTracker,
            @NotNull BoundedTracker<Window> windowTracker, @NotNull PowerupHandler powerupHandler,
            @NotNull MobStore mobStore) {
        EventNode<Event> node = EventNode.all("phantazm_zombies_instance_{" + instance.getUniqueId() + "}");
        MapSettingsInfo settings = mapInfo.settings();

        //entity events
        node.addListener(EntityDeathEvent.class,
                new PhantazmMobDeathListener(keyParser, instance, mobStore, roundHandler::currentRound, powerupHandler,
                        roomTracker, windowTracker));
        node.addListener(EntityDamageEvent.class, new PlayerDamageMobListener(instance, mobStore, zombiesPlayers));
        node.addListener(EntityDamageByGunEvent.class,
                new EntityDamageByGunEventListener(instance, mobStore, mapObjects, zombiesPlayers));

        //player events
        node.addListener(EntityDamageEvent.class, new PlayerDamageEventListener(instance, zombiesPlayers, mapObjects));
        node.addListener(PlayerHandAnimationEvent.class,
                new PlayerLeftClickListener(instance, zombiesPlayers, mobStore, settings.punchDamage(),
                        settings.punchRange()));
        node.addListener(PlayerChangeHeldSlotEvent.class, new PlayerItemSelectListener(instance, zombiesPlayers));
        node.addListener(ItemDropEvent.class, new PlayerDropItemListener(instance, zombiesPlayers));
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(instance, zombiesPlayers));

        //various forms of clicking
        PlayerRightClickListener rightClickListener = new PlayerRightClickListener();
        node.addListener(PlayerBlockInteractEvent.class,
                new PlayerInteractBlockListener(instance, zombiesPlayers, shopHandler, doorHandler,
                        rightClickListener));
        node.addListener(PlayerEntityInteractEvent.class,
                new PlayerInteractEntityListener(instance, zombiesPlayers, shopHandler, rightClickListener));
        node.addListener(PlayerUseItemEvent.class,
                new PlayerUseItemListener(instance, zombiesPlayers, rightClickListener));
        node.addListener(PlayerUseItemOnBlockEvent.class,
                new PlayerUseItemOnBlockListener(instance, zombiesPlayers, rightClickListener));
        node.addListener(PlayerSwapItemEvent.class, new PlayerSwapItemListener(instance, zombiesPlayers));

        //sneaking/not sneaking
        node.addListener(PlayerStartSneakingEvent.class,
                new PlayerStartSneakingListener(instance, zombiesPlayers, windowHandler));
        node.addListener(PlayerStopSneakingEvent.class,
                new PlayerStopSneakingListener(instance, zombiesPlayers, windowHandler));

        //inventory
        node.addListener(InventoryPreClickEvent.class, new PlayerInventoryPreClickListener(instance, zombiesPlayers));
        node.addListener(PlayerPreEatEvent.class, new PlayerEatItemEventListener(instance, zombiesPlayers));

        for (EventTrigger<?> trigger : EventTriggers.TRIGGERS) {
            trigger.initialize(node, mobStore);
        }

        return node;
    }

    private @NotNull StageTransition createStageTransition(@NotNull Instance instance,
            @NotNull List<Component> messages, @NotNull Random random,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Pos spawnPos,
            @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull SidebarModule sidebarModule, @NotNull ShopHandler shopHandler) {
        Stage idle = new IdleStage(zombiesPlayers);

        LongList alertTicks = LongList.of(400L, 200L, 100L, 80L, 60L, 40L, 20L);
        TickFormatter tickFormatter =
                new DurationTickFormatter(new DurationTickFormatter.Data(NamedTextColor.YELLOW, true, false));

        Stage countdown =
                new CountdownStage(instance, zombiesPlayers, messages, random, 400L, alertTicks, tickFormatter,
                        newSidebarUpdaterCreator(sidebarModule, ElementPath.of("countdown")));

        MapSettingsInfo settings = mapInfo.settings();
        Stage inGame = new InGameStage(instance, zombiesPlayers, spawnPos, roundHandler, ticksSinceStart,
                settings.defaultEquipment(), settings.equipmentGroups().keySet(),
                newSidebarUpdaterCreator(sidebarModule, ElementPath.of("inGame")), shopHandler);
        Stage end = new EndStage(instance, zombiesPlayers, 200L,
                newSidebarUpdaterCreator(sidebarModule, ElementPath.of("end")));
        return new StageTransition(idle, countdown, inGame, end);
    }

    private @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> newSidebarUpdaterCreator(
            @NotNull SidebarModule sidebarModule, @NotNull ElementPath scoreboardSubNode) {
        ElementContext context = contextManager.makeContext(mapInfo.scoreboard());
        return new ElementSidebarUpdaterCreator(sidebarModule, context, keyParser, scoreboardSubNode);
    }

    private record SceneContext(@NotNull EventNode<?> node) {

        public SceneContext {
            Objects.requireNonNull(node, "node");
        }

    }
}
