package org.phantazm.zombies.scene2;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.proxima.solid.Solid;
import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.BelowNameTag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.tick.BasicTickTaskScheduler;
import org.phantazm.core.tick.TickTaskScheduler;
import org.phantazm.commons.flag.BasicFlaggable;
import org.phantazm.core.ClientBlockHandlerSource;
import org.phantazm.core.VecUtils;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.sound.BasicSongPlayer;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.core.time.AnalogTickFormatter;
import org.phantazm.core.time.PrecisionSecondTickFormatter;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.corpse.CorpseCreator;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.listener.*;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.handler.*;
import org.phantazm.zombies.map.objects.BasicMapObjectsSource;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.mob2.MobSpawnerSource;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.sidebar.ElementSidebarUpdaterCreator;
import org.phantazm.zombies.sidebar.SidebarModule;
import org.phantazm.zombies.sidebar.SidebarUpdater;
import org.phantazm.zombies.stage.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZombiesSceneCreator implements SceneCreator<ZombiesScene> {
    private final int sceneCap;
    private final Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction;
    private final MapInfo mapInfo;
    private final InstanceLoader instanceLoader;
    private final EventNode<Event> rootNode;
    private final ContextManager contextManager;
    private final KeyParser keyParser;
    private final ZombiesDatabase database;

    private final MapObjects.Source mapObjectSource;
    private final ZombiesPlayer.Source zombiesPlayerSource;
    private final PowerupHandler.Source powerupHandlerSource;
    private final ShopHandler.Source shopHandlerSource;
    private final WindowHandler.Source windowHandlerSource;
    private final DoorHandler.Source doorHandlerSource;
    private final CorpseCreator.Source corpseCreatorSource;
    private final SongLoader songLoader;

    public ZombiesSceneCreator(int sceneCap,
        @NotNull MapInfo mapInfo, @NotNull InstanceLoader instanceLoader, @NotNull KeyParser keyParser, @NotNull ContextManager contextManager, @NotNull SongLoader songLoader, @NotNull ZombiesDatabase database, @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction,
        @NotNull EventNode<Event> rootNode, @NotNull MobSpawnerSource mobSpawnerSource,
        @NotNull ClientBlockHandlerSource clientBlockHandlerSource,
        @NotNull PowerupHandler.Source powerupHandlerSource,
        @NotNull ZombiesPlayer.Source zombiesPlayerSource, @NotNull CorpseCreator.Source corpseCreatorSource) {
        this.sceneCap = sceneCap;
        this.instanceSpaceFunction = Objects.requireNonNull(instanceSpaceFunction);
        this.mapInfo = Objects.requireNonNull(mapInfo);
        this.instanceLoader = Objects.requireNonNull(instanceLoader);
        this.rootNode = Objects.requireNonNull(rootNode);
        this.contextManager = Objects.requireNonNull(contextManager);
        this.keyParser = Objects.requireNonNull(keyParser);
        this.database = Objects.requireNonNull(database);

        MapSettingsInfo settingsInfo = mapInfo.settings();

        this.mapObjectSource = new BasicMapObjectsSource(mapInfo, contextManager, mobSpawnerSource,
            clientBlockHandlerSource, keyParser);
        this.zombiesPlayerSource = Objects.requireNonNull(zombiesPlayerSource);
        this.powerupHandlerSource = Objects.requireNonNull(powerupHandlerSource);
        this.shopHandlerSource = new BasicShopHandlerSource();
        this.windowHandlerSource =
            new BasicWindowHandlerSource(settingsInfo.windowRepairRadius(), settingsInfo.windowRepairTicks(),
                settingsInfo.repairCoins(), new WindowHandler.WindowMessages(settingsInfo.nearWindowMessage(),
                settingsInfo.startRepairingMessage(), settingsInfo.stopRepairingMessage(),
                settingsInfo.finishRepairingMessage(), settingsInfo.enemiesNearbyMessage()));
        this.doorHandlerSource = new BasicDoorHandlerSource();

        this.corpseCreatorSource = Objects.requireNonNull(corpseCreatorSource);
        this.songLoader = Objects.requireNonNull(songLoader);
    }

    @Override
    public @NotNull ZombiesScene createScene() {
        Wrapper<RoundHandler> roundHandlerWrapper = Wrapper.ofNull();
        Wrapper<PowerupHandler> powerupHandlerWrapper = Wrapper.ofNull();
        Wrapper<WindowHandler> windowHandlerWrapper = Wrapper.ofNull();
        Wrapper<EventNode<Event>> eventNodeWrapper = Wrapper.ofNull();
        Wrapper<Long> ticksSinceStart = Wrapper.of(0L);
        Wrapper<ZombiesScene> sceneWrapper = Wrapper.ofNull();

        MapSettingsInfo settings = mapInfo.settings();
        Pos spawnPos = VecUtils.toPos(settings.origin().add(settings.spawn())).add(0.5, 0, 0.5);

        Instance instance = instanceLoader.loadInstance(settings.instancePath()).join();

        instance.setTime(settings.worldTime());
        instance.setTimeRate(0);

        Map<PlayerView, ZombiesPlayer> zombiesPlayers = new ConcurrentHashMap<>(settings.maxPlayers());
        TickTaskScheduler tickTaskScheduler = new BasicTickTaskScheduler();

        SongPlayer songPlayer = new BasicSongPlayer();

        MapObjects mapObjects =
            createMapObjects(sceneWrapper.unmodifiableView(), instance, zombiesPlayers, roundHandlerWrapper,
                powerupHandlerWrapper, windowHandlerWrapper, eventNodeWrapper,
                songPlayer, songLoader, tickTaskScheduler, ticksSinceStart);

        RoundHandler roundHandler = new BasicRoundHandler(zombiesPlayers.values(), mapObjects.rounds());
        roundHandlerWrapper.set(roundHandler);

        PowerupHandler powerupHandler = powerupHandlerSource.make(sceneWrapper.unmodifiableView());
        powerupHandlerWrapper.set(powerupHandler);

        ShopHandler shopHandler = createShopHandler(mapObjects.shopTracker(), mapObjects.roomTracker());

        WindowHandler windowHandler =
            createWindowHandler(mapObjects.windowTracker(), mapObjects.roomTracker(), zombiesPlayers.values());
        windowHandlerWrapper.set(windowHandler);

        DoorHandler doorHandler = createDoorHandler(mapObjects.doorTracker(), mapObjects.roomTracker(),
            sceneWrapper.unmodifiableView());

        ZombiesMap map =
            new ZombiesMap(mapObjects, songPlayer, powerupHandler, roundHandler, shopHandler, windowHandler,
                doorHandler);

        SidebarModule sidebarModule =
            new SidebarModule(zombiesPlayers, zombiesPlayers.values(), roundHandler, ticksSinceStart,
                settings.maxPlayers());
        StageTransition stageTransition =
            createStageTransition(instance, mapObjects.module().random(), zombiesPlayers.values(), spawnPos,
                roundHandler, ticksSinceStart, sidebarModule, shopHandler, sceneWrapper.unmodifiableView());
        stageTransition.start();

        EventNode<Event> childNode =
            createEventNode(sceneWrapper.unmodifiableView(), instance, zombiesPlayers, mapObjects, roundHandler,
                shopHandler, windowHandler, doorHandler, mapObjects.roomTracker(), mapObjects.windowTracker(),
                powerupHandler, spawnPos);
        eventNodeWrapper.set(childNode);

        CorpseCreator corpseCreator = createCorpseCreator(mapObjects.mapDependencyProvider());
        BelowNameTag belowNameTag = new BelowNameTag(UUID.randomUUID().toString(), settings.healthDisplay());
        Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator = playerView -> {
            return zombiesPlayerSource.createPlayer(sceneWrapper.get(), zombiesPlayers, settings,
                mapInfo.playerCoins(), mapInfo.leaderboard(), instance, playerView,
                mapObjects.module().modifierSource(), new BasicFlaggable(), childNode,
                mapObjects.module().random(), mapObjects, mapObjects.mobSpawner(), corpseCreator,
                belowNameTag);
        };

        ZombiesScene scene = new ZombiesScene(instance, map, settings, zombiesPlayers, stageTransition, playerCreator,
            database, childNode, tickTaskScheduler);
        sceneWrapper.set(scene);
        rootNode.addChild(childNode);

        InstanceSpawner.InstanceSettings instanceSettings = instanceSpaceFunction.apply(instance);
        instanceSettings.spaceHandler().space().setOverrideFunction((x, y, z) -> {
            if (windowHandler.tracker().atPoint(x, y, z).isPresent()) {
                return Solid.EMPTY;
            }

            return null;
        });

        map.objects().mobSpawner().init();
        return scene;
    }

    @Override
    public int sceneCap() {
        return sceneCap;
    }

    @Override
    public int playerCap() {
        return mapInfo.settings().maxPlayers();
    }

    @Override
    public boolean hasPermission(@NotNull Set<? extends @NotNull PlayerView> players) {
        List<Player> onlinePlayers = PlayerView.getMany(players, ArrayList::new);
        for (String permission : mapInfo.settings().requiredPermissions()) {
            for (Player player : onlinePlayers) {
                if (!player.hasPermission(permission)) {
                    return false;
                }
            }
        }

        return true;
    }

    private CorpseCreator createCorpseCreator(DependencyProvider mapDependencyProvider) {
        return corpseCreatorSource.make(mapDependencyProvider);
    }

    private MapObjects createMapObjects(Supplier<ZombiesScene> scene, Instance instance,
        Map<PlayerView, ZombiesPlayer> zombiesPlayers,
        Supplier<? extends RoundHandler> roundHandlerSupplier,
        Wrapper<PowerupHandler> powerupHandler, Wrapper<WindowHandler> windowHandler,
        Wrapper<EventNode<Event>> eventNode, SongPlayer songPlayer, SongLoader songLoader,
        TickTaskScheduler tickTaskScheduler, Wrapper<Long> ticksSinceStart) {
        return mapObjectSource.make(scene, instance, zombiesPlayers, roundHandlerSupplier,
            powerupHandler, windowHandler, eventNode, songPlayer, songLoader, tickTaskScheduler,
            ticksSinceStart);
    }

    private ShopHandler createShopHandler(BoundedTracker<Shop> shopTracker, BoundedTracker<Room> rooms) {
        return shopHandlerSource.make(shopTracker, rooms);
    }

    private WindowHandler createWindowHandler(BoundedTracker<Window> windowTracker, BoundedTracker<Room> roomTracker,
        Collection<? extends ZombiesPlayer> players) {
        return windowHandlerSource.make(windowTracker, roomTracker, players);
    }

    private DoorHandler createDoorHandler(BoundedTracker<Door> doorTracker, BoundedTracker<Room> roomTracker,
        Supplier<ZombiesScene> zombiesScene) {
        return doorHandlerSource.make(doorTracker, roomTracker, zombiesScene);
    }

    private @NotNull EventNode<Event> createEventNode(@NotNull Supplier<ZombiesScene> sceneSupplier, @NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull MapObjects mapObjects,
        @NotNull RoundHandler roundHandler, @NotNull ShopHandler shopHandler, @NotNull WindowHandler windowHandler,
        @NotNull DoorHandler doorHandler, @NotNull BoundedTracker<Room> roomTracker,
        @NotNull BoundedTracker<Window> windowTracker, @NotNull PowerupHandler powerupHandler, @NotNull Pos spawnPos) {
        EventNode<Event> node = EventNode.all("phantazm_zombies_instance_" + instance.getUniqueId());
        MapSettingsInfo settings = mapInfo.settings();

        //entity events
        node.addListener(EntityDeathEvent.class,
            new PhantazmMobDeathListener(keyParser, instance, roundHandler::currentRound, powerupHandler,
                roomTracker, windowTracker, zombiesPlayers, settings, sceneSupplier));
        node.addListener(EntityDamageByGunEvent.class,
            new EntityDamageByGunEventListener(instance, mapObjects, zombiesPlayers, sceneSupplier));

        //player events
        node.addListener(EntityDamageEvent.class,
            new PlayerDamageEventListener(instance, zombiesPlayers, mapObjects, settings, sceneSupplier));
        node.addListener(PlayerHandAnimationEvent.class, new PlayerLeftClickListener(instance, zombiesPlayers, sceneSupplier));
        node.addListener(PlayerBlockInteractEvent.class, new PlayerBlockInteractListener(instance, zombiesPlayers, sceneSupplier));

        node.addListener(EntityAttackEvent.class, new PlayerAttackEntityListener(instance, zombiesPlayers,
            settings.punchDamage(), settings.punchCooldown(), settings.punchKnockback(), sceneSupplier));
        node.addListener(PlayerChangeHeldSlotEvent.class, new PlayerItemSelectListener(instance, zombiesPlayers, sceneSupplier));
        node.addListener(ItemDropEvent.class, new PlayerDropItemListener(instance, zombiesPlayers, sceneSupplier));

        //various forms of clicking
        PlayerRightClickListener rightClickListener = new PlayerRightClickListener();
        node.addListener(PlayerBlockInteractEvent.class,
            new PlayerInteractBlockListener(instance, zombiesPlayers, shopHandler, doorHandler,
                rightClickListener, sceneSupplier));
        node.addListener(PlayerEntityInteractEvent.class,
            new PlayerInteractEntityListener(instance, zombiesPlayers, shopHandler, rightClickListener, sceneSupplier));
        node.addListener(PlayerUseItemEvent.class,
            new PlayerUseItemListener(instance, zombiesPlayers, rightClickListener, sceneSupplier));
        node.addListener(PlayerUseItemOnBlockEvent.class,
            new PlayerUseItemOnBlockListener(instance, zombiesPlayers, rightClickListener, sceneSupplier));
        node.addListener(PlayerSwapItemEvent.class, new PlayerSwapItemListener(instance, zombiesPlayers, sceneSupplier));

        //sneaking/not sneaking
        node.addListener(PlayerStartSneakingEvent.class,
            new PlayerStartSneakingListener(instance, zombiesPlayers, windowHandler, sceneSupplier));
        node.addListener(PlayerStopSneakingEvent.class,
            new PlayerStopSneakingListener(instance, zombiesPlayers, windowHandler, sceneSupplier));

        //inventory
        node.addListener(InventoryPreClickEvent.class, new PlayerInventoryPreClickListener(instance, zombiesPlayers, sceneSupplier));
        node.addListener(PlayerPreEatEvent.class, new PlayerEatItemEventListener(instance, zombiesPlayers, sceneSupplier));

        node.addListener(PlayerRespawnEvent.class, new PlayerRespawnListener(instance, zombiesPlayers, spawnPos, sceneSupplier));

        return node;
    }

    private @NotNull StageTransition createStageTransition(@NotNull Instance instance, @NotNull Random random,
        @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Pos spawnPos,
        @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
        @NotNull SidebarModule sidebarModule, @NotNull ShopHandler shopHandler, @NotNull Supplier<ZombiesScene> sceneSupplier) {
        MapSettingsInfo settings = mapInfo.settings();

        Stage idle = new IdleStage(instance, settings, zombiesPlayers,
            newSidebarUpdaterCreator(sidebarModule, ElementPath.of("idle")), settings.idleRevertTicks());

        LongList countdownAlertTicks = new LongArrayList(settings.countdownAlertTicks());

        Stage countdown = new CountdownStage(instance, zombiesPlayers, settings, random, settings.countdownTicks(),
            countdownAlertTicks, new PrecisionSecondTickFormatter(new PrecisionSecondTickFormatter.Data(0)),
            newSidebarUpdaterCreator(sidebarModule, ElementPath.of("countdown")));

        Stage inGame =
            new InGameStage(zombiesPlayers, spawnPos, roundHandler, ticksSinceStart, settings.defaultEquipment(),
                settings.equipmentGroups().keySet(),
                newSidebarUpdaterCreator(sidebarModule, ElementPath.of("inGame")), shopHandler);

        Stage end = new EndStage(instance, settings, mapInfo.webhook(),
            new AnalogTickFormatter(new AnalogTickFormatter.Data(false)), zombiesPlayers,
            Wrapper.of(settings.endTicks()), ticksSinceStart,
            (player, hasWon) -> {
                return newSidebarUpdaterCreator(sidebarModule, ElementPath.of(hasWon ? "win" : "lose")).apply(player);
            }, roundHandler, database, sceneSupplier);
        return new StageTransition(idle, countdown, inGame, end);
    }

    private @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> newSidebarUpdaterCreator(
        @NotNull SidebarModule sidebarModule, @NotNull ElementPath scoreboardSubNode) {
        ElementContext context = contextManager.makeContext(mapInfo.scoreboard());
        return new ElementSidebarUpdaterCreator(sidebarModule, context, keyParser, scoreboardSubNode);
    }
}