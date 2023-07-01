package org.phantazm.zombies.scene;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.proxima.solid.Solid;
import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicTickTaskScheduler;
import org.phantazm.commons.TickTaskScheduler;
import org.phantazm.core.ClientBlockHandlerSource;
import org.phantazm.core.VecUtils;
import org.phantazm.core.game.scene.SceneProviderAbstract;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.hologram.ViewableHologram;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.sound.BasicSongPlayer;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.core.time.AnalogTickFormatter;
import org.phantazm.core.time.PrecisionSecondTickFormatter;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.trigger.EventTrigger;
import org.phantazm.mob.trigger.EventTriggers;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.corpse.CorpseCreator;
import org.phantazm.zombies.event.EntityDamageByGunEvent;
import org.phantazm.zombies.leaderboard.BestTimeLeaderboard;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZombiesSceneProvider extends SceneProviderAbstract<ZombiesScene, ZombiesJoinRequest> {
    private final Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction;
    private final MapInfo mapInfo;
    private final InstanceLoader instanceLoader;
    private final SceneFallback sceneFallback;
    private final EventNode<Event> rootNode;
    private final ContextManager contextManager;
    private final KeyParser keyParser;
    private final Team mobNoPushTeam;
    private final Team corpseTeam;
    private final ZombiesDatabase database;

    private final MapObjects.Source mapObjectSource;
    private final ZombiesPlayer.Source zombiesPlayerSource;
    private final PowerupHandler.Source powerupHandlerSource;
    private final ShopHandler.Source shopHandlerSource;
    private final WindowHandler.Source windowHandlerSource;
    private final DoorHandler.Source doorHandlerSource;
    private final CorpseCreator.Source corpseCreatorSource;
    private final PlayerViewProvider viewProvider;
    private final SongLoader songLoader;

    public ZombiesSceneProvider(@NotNull Executor executor, int maximumScenes,
            @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction,
            @NotNull MapInfo mapInfo, @NotNull InstanceLoader instanceLoader, @NotNull SceneFallback sceneFallback,
            @NotNull EventNode<Event> rootNode, @NotNull MobSpawnerSource mobSpawnerSource,
            @NotNull Map<Key, MobModel> mobModels, @NotNull ClientBlockHandlerSource clientBlockHandlerSource,
            @NotNull ContextManager contextManager, @NotNull KeyParser keyParser, @NotNull Team mobNoPushTeam,
            @NotNull Team corpseTeam, @NotNull ZombiesDatabase database, @NotNull Map<Key, PowerupInfo> powerups,
            @NotNull ZombiesPlayer.Source zombiesPlayerSource, @NotNull CorpseCreator.Source corpseCreatorSource,
            @NotNull PlayerViewProvider viewProvider, @NotNull SongLoader songLoader) {
        super(executor, maximumScenes);
        this.instanceSpaceFunction = Objects.requireNonNull(instanceSpaceFunction, "instanceSpaceFunction");
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.sceneFallback = Objects.requireNonNull(sceneFallback, "sceneFallback");
        this.rootNode = Objects.requireNonNull(rootNode, "eventNode");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.database = Objects.requireNonNull(database, "database");
        Objects.requireNonNull(powerups, "powerups");

        MapSettingsInfo settingsInfo = mapInfo.settings();
        this.mobNoPushTeam = settingsInfo.mobPlayerCollisions() ? null : mobNoPushTeam;
        this.corpseTeam = Objects.requireNonNull(corpseTeam, "corpseTeam");

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

        this.corpseCreatorSource = Objects.requireNonNull(corpseCreatorSource, "corpseCreatorSource");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.songLoader = Objects.requireNonNull(songLoader, "songLoader");
    }

    @Override
    protected @NotNull Optional<ZombiesScene> chooseScene(@NotNull ZombiesJoinRequest request) {
        sceneLoop:
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

            if (currentPlayerCount + request.getPlayers().size() > maxPlayers) {
                continue;
            }

            for (PlayerView view : request.getPlayers()) {
                if (scene.getZombiesPlayers().containsKey(view.getUUID())) {
                    continue sceneLoop;
                }
            }

            return Optional.of(scene);
        }

        return Optional.empty();
    }

    @Override
    protected @NotNull CompletableFuture<ZombiesScene> createScene(@NotNull ZombiesJoinRequest request) {
        Wrapper<RoundHandler> roundHandlerWrapper = Wrapper.ofNull();
        Wrapper<PowerupHandler> powerupHandlerWrapper = Wrapper.ofNull();
        Wrapper<WindowHandler> windowHandlerWrapper = Wrapper.ofNull();
        Wrapper<EventNode<Event>> eventNodeWrapper = Wrapper.ofNull();
        Wrapper<Long> ticksSinceStart = Wrapper.of(0L);
        Wrapper<ZombiesScene> sceneWrapper = Wrapper.ofNull();

        MapSettingsInfo settings = mapInfo.settings();
        Pos spawnPos = VecUtils.toPos(settings.origin().add(settings.spawn()));

        return instanceLoader.loadInstance(settings.instancePath()).thenApply(instance -> {
            instance.setTime(settings.worldTime());
            instance.setTimeRate(0);

            //LinkedHashMap for better value iteration performance
            Map<UUID, ZombiesPlayer> zombiesPlayers = new ConcurrentHashMap<>(settings.maxPlayers());

            MobStore mobStore = new MobStore();
            TickTaskScheduler tickTaskScheduler = new BasicTickTaskScheduler();

            SongPlayer songPlayer = new BasicSongPlayer();
            MapObjects mapObjects =
                    createMapObjects(instance, zombiesPlayers, roundHandlerWrapper, mobStore, mobNoPushTeam, corpseTeam,
                            powerupHandlerWrapper, windowHandlerWrapper, eventNodeWrapper, songPlayer, songLoader,
                            tickTaskScheduler, ticksSinceStart);

            RoundHandler roundHandler = new BasicRoundHandler(zombiesPlayers.values(), mapObjects.rounds());
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


            SidebarModule sidebarModule =
                    new SidebarModule(zombiesPlayers, zombiesPlayers.values(), roundHandler, ticksSinceStart,
                            settings.maxPlayers());
            StageTransition stageTransition =
                    createStageTransition(instance, mapObjects.module().random(), zombiesPlayers.values(), spawnPos,
                            roundHandler, ticksSinceStart, sidebarModule, shopHandler);
            stageTransition.start();

            LeaveHandler leaveHandler = new LeaveHandler(stageTransition, zombiesPlayers);


            EventNode<Event> childNode =
                    createEventNode(instance, zombiesPlayers, mapObjects, roundHandler, shopHandler, windowHandler,
                            doorHandler, mapObjects.roomTracker(), mapObjects.windowTracker(), powerupHandler, mobStore,
                            leaveHandler);
            eventNodeWrapper.set(childNode);

            CorpseCreator corpseCreator = createCorpseCreator(mapObjects.mapDependencyProvider());


            Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator = playerView -> {
                playerView.getPlayer().ifPresent(player -> {
                    player.getAttribute(Attributes.HEAL_TICKS).setBaseValue(settings.healTicks());
                    player.getAttribute(Attributes.REVIVE_TICKS).setBaseValue(settings.baseReviveTicks());
                });

                return zombiesPlayerSource.createPlayer(sceneWrapper.get(), zombiesPlayers, settings,
                        mapInfo.playerCoins(), mapInfo.leaderboard(), instance, playerView,
                        mapObjects.module().modifierSource(), new BasicFlaggable(), childNode,
                        mapObjects.module().random(), mapObjects, mobStore, mapObjects.mobSpawner(), corpseCreator);
            };

            ZombiesScene scene =
                    new ZombiesScene(UUID.randomUUID(), map, zombiesPlayers, instance, sceneFallback, settings,
                            stageTransition, leaveHandler, playerCreator, tickTaskScheduler, database, childNode);
            sceneWrapper.set(scene);
            rootNode.addChild(childNode);

            InstanceSpawner.InstanceSettings instanceSettings = instanceSpaceFunction.apply(instance);
            instanceSettings.spaceHandler().space().setOverrideFunction((x, y, z) -> {
                if (windowHandler.tracker().atPoint(x, y, z).isPresent()) {
                    return Solid.EMPTY;
                }

                return null;
            });

            return scene;
        });
    }

    @Override
    protected void cleanupScene(@NotNull ZombiesScene scene) {
        rootNode.removeChild(scene.getSceneNode());
    }

    private CorpseCreator createCorpseCreator(DependencyProvider mapDependencyProvider) {
        return corpseCreatorSource.make(mapDependencyProvider);
    }

    private MapObjects createMapObjects(Instance instance, Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            Supplier<? extends RoundHandler> roundHandlerSupplier, MobStore mobStore, Team mobNoPushTeam,
            Team corpseTeam, Wrapper<PowerupHandler> powerupHandler, Wrapper<WindowHandler> windowHandler,
            Wrapper<EventNode<Event>> eventNode, SongPlayer songPlayer, SongLoader songLoader,
            TickTaskScheduler tickTaskScheduler, Wrapper<Long> ticksSinceStart) {
        return mapObjectSource.make(instance, zombiesPlayers, roundHandlerSupplier, mobStore, mobNoPushTeam,
                powerupHandler, windowHandler, eventNode, songPlayer, songLoader, tickTaskScheduler, corpseTeam,
                ticksSinceStart);
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
            @NotNull MobStore mobStore, @NotNull LeaveHandler leaveHandler) {
        EventNode<Event> node = EventNode.all("phantazm_zombies_instance_{" + instance.getUniqueId() + "}");
        MapSettingsInfo settings = mapInfo.settings();

        //entity events
        node.addListener(EntityDeathEvent.class,
                new PhantazmMobDeathListener(keyParser, instance, mobStore, roundHandler::currentRound, powerupHandler,
                        roomTracker, windowTracker, zombiesPlayers));
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
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(instance, zombiesPlayers, leaveHandler));

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

    private @NotNull StageTransition createStageTransition(@NotNull Instance instance, @NotNull Random random,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Pos spawnPos,
            @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull SidebarModule sidebarModule, @NotNull ShopHandler shopHandler) {
        MapSettingsInfo settings = mapInfo.settings();

        Stage idle = new IdleStage(zombiesPlayers, newSidebarUpdaterCreator(sidebarModule, ElementPath.of("idle")),
                settings.idleRevertTicks());

        LongList countdownAlertTicks = new LongArrayList(settings.countdownAlertTicks());

        Stage countdown = new CountdownStage(instance, zombiesPlayers, settings, random, settings.countdownTicks(),
                countdownAlertTicks, new PrecisionSecondTickFormatter(new PrecisionSecondTickFormatter.Data(0)),
                newSidebarUpdaterCreator(sidebarModule, ElementPath.of("countdown")));

        Stage inGame =
                new InGameStage(zombiesPlayers, spawnPos, roundHandler, ticksSinceStart, settings.defaultEquipment(),
                        settings.equipmentGroups().keySet(),
                        newSidebarUpdaterCreator(sidebarModule, ElementPath.of("inGame")), shopHandler);

        Stage end = new EndStage(instance, settings, new AnalogTickFormatter(new AnalogTickFormatter.Data(false)),
                zombiesPlayers, Wrapper.of(settings.endTicks()), ticksSinceStart,
                newSidebarUpdaterCreator(sidebarModule, ElementPath.of("end")), roundHandler);
        return new StageTransition(idle, countdown, inGame, end);
    }

    private @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> newSidebarUpdaterCreator(
            @NotNull SidebarModule sidebarModule, @NotNull ElementPath scoreboardSubNode) {
        ElementContext context = contextManager.makeContext(mapInfo.scoreboard());
        return new ElementSidebarUpdaterCreator(sidebarModule, context, keyParser, scoreboardSubNode);
    }
}
