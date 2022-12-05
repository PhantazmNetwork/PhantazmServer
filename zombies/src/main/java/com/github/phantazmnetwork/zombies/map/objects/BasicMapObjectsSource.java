package com.github.phantazmnetwork.zombies.map.objects;

import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.ClientBlockHandlerSource;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.core.gui.BasicSlotDistributor;
import com.github.phantazmnetwork.core.gui.SlotDistributor;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.coin.BasicTransactionModifierSource;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.phantazmnetwork.zombies.map.action.Action;
import com.github.phantazmnetwork.zombies.map.shop.Shop;
import com.github.phantazmnetwork.zombies.map.shop.display.ShopDisplay;
import com.github.phantazmnetwork.zombies.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.spawn.BasicSpawnDistributor;
import com.github.phantazmnetwork.zombies.spawn.SpawnDistributor;
import com.github.phantazmnetwork.zombies.util.ElementUtils;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.toolkit.collection.Wrapper;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class BasicMapObjectsSource implements MapObjects.Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMapObjectsSource.class);

    private final MapInfo mapInfo;
    private final ContextManager contextManager;
    private final MobSpawner mobSpawner;
    private final Map<Key, MobModel> mobModels;
    private final ClientBlockHandlerSource clientBlockHandlerSource;
    private final KeyParser keyParser;

    public BasicMapObjectsSource(@NotNull MapInfo mapInfo, @NotNull ContextManager contextManager,
            @NotNull MobSpawner mobSpawner, @NotNull Map<Key, MobModel> mobModels,
            @NotNull ClientBlockHandlerSource clientBlockHandlerSource, @NotNull KeyParser keyParser) {
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobModels = Objects.requireNonNull(mobModels, "mobModels");
        this.clientBlockHandlerSource = Objects.requireNonNull(clientBlockHandlerSource, "clientBlockHandlerSource");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    public @NotNull MapObjects make(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier, @NotNull MobStore mobStore) {
        Random random = new Random();
        ClientBlockHandler clientBlockHandler = clientBlockHandlerSource.forInstance(instance);
        SpawnDistributor spawnDistributor = new BasicSpawnDistributor(mobModels::get, random, playerMap.values());

        Flaggable flaggable = new BasicFlaggable();
        TransactionModifierSource transactionModifierSource = new BasicTransactionModifierSource();
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);

        MapSettingsInfo mapSettingsInfo = mapInfo.settings();
        Pos respawnPos =
                new Pos(VecUtils.toPoint(mapSettingsInfo.origin().add(mapSettingsInfo.spawn())), mapSettingsInfo.yaw(),
                        mapSettingsInfo.pitch());

        Wrapper<MapObjects> mapObjectsWrapper = Wrapper.ofNull();

        Module module = new Module(instance, random, roundHandlerSupplier, flaggable, transactionModifierSource,
                slotDistributor, playerMap, respawnPos, mapObjectsWrapper);

        DependencyProvider provider = new ModuleDependencyProvider(keyParser, module);

        Point origin = VecUtils.toPoint(mapSettingsInfo.origin());

        Map<Key, SpawnruleInfo> spawnruleInfoMap = buildSpawnrules(mapInfo.spawnrules());
        List<Spawnpoint> spawnpoints =
                buildSpawnpoints(origin, mapInfo.spawnpoints(), spawnruleInfoMap, instance, mobStore);
        List<Window> windows = buildWindows(origin, mapInfo.windows(), provider, instance, clientBlockHandler);
        List<Shop> shops = buildShops(mapInfo.shops(), provider, instance);
        List<Door> doors = buildDoors(origin, mapInfo.doors(), provider, instance);
        List<Room> rooms = buildRooms(origin, mapInfo.rooms(), provider);
        List<Round> rounds = buildRounds(mapInfo.rounds(), spawnpoints, provider, spawnDistributor);

        MapObjects mapObjects =
                new BasicMapObjects(spawnpoints, windows, shops, doors, rooms, rounds, provider, module);
        mapObjectsWrapper.set(mapObjects);

        return mapObjects;
    }

    private Map<Key, SpawnruleInfo> buildSpawnrules(List<SpawnruleInfo> spawnruleInfoList) {
        Map<Key, SpawnruleInfo> spawnruleInfoMap = new HashMap<>(spawnruleInfoList.size());
        for (SpawnruleInfo spawnruleInfo : spawnruleInfoList) {
            if (spawnruleInfoMap.putIfAbsent(spawnruleInfo.id(), spawnruleInfo) != null) {
                LOGGER.warn("Spawnrule found with duplicate id '{}'", spawnruleInfo.id());
            }
        }

        return spawnruleInfoMap;
    }

    private List<Spawnpoint> buildSpawnpoints(Point mapOrigin, List<SpawnpointInfo> spawnpointInfoList,
            Map<Key, SpawnruleInfo> spawnruleInfoMap, Instance instance, MobStore mobStore) {
        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointInfoList.size());
        for (SpawnpointInfo spawnpointInfo : spawnpointInfoList) {
            spawnpoints.add(
                    new Spawnpoint(mapOrigin, spawnpointInfo, instance, spawnruleInfoMap::get, mobStore, mobSpawner));
        }

        return spawnpoints;
    }

    private List<Window> buildWindows(Point mapOrigin, List<WindowInfo> windowInfoList,
            DependencyProvider dependencyProvider, Instance instance, ClientBlockHandler clientBlockHandler) {
        List<Window> windows = new ArrayList<>(windowInfoList.size());
        for (WindowInfo windowInfo : windowInfoList) {
            ConfigList repairActionInfo = windowInfo.repairActions();
            ConfigList breakActionInfo = windowInfo.breakActions();

            List<Action<Window>> repairActions = new ArrayList<>(repairActionInfo.size());
            List<Action<Window>> breakActions = new ArrayList<>(breakActionInfo.size());

            ElementUtils.createElements(contextManager, repairActionInfo, repairActions, "window repair action",
                    dependencyProvider, LOGGER);
            ElementUtils.createElements(contextManager, breakActionInfo, breakActions, "window break action",
                    dependencyProvider, LOGGER);

            windows.add(new Window(mapOrigin, instance, windowInfo, clientBlockHandler, repairActions, breakActions));
        }

        return windows;
    }

    private List<Shop> buildShops(List<ShopInfo> shopInfoList, DependencyProvider dependencyProvider,
            Instance instance) {
        List<Shop> shops = new ArrayList<>(shopInfoList.size());
        for (ShopInfo shopInfo : shopInfoList) {
            ConfigNode rootNode = shopInfo.data();
            ElementContext shopContext = contextManager.makeContext(rootNode);

            ConfigList predicateInfo = ElementUtils.extractList(rootNode, LOGGER, "predicates");
            ConfigList successInteractorInfo = ElementUtils.extractList(rootNode, LOGGER, "successInteractors");
            ConfigList failureInteractorInfo = ElementUtils.extractList(rootNode, LOGGER, "failureInteractors");
            ConfigList displayInfo = ElementUtils.extractList(rootNode, LOGGER, "displays");

            List<ShopPredicate> predicates = new ArrayList<>(predicateInfo.size());
            List<ShopInteractor> successInteractors = new ArrayList<>(successInteractorInfo.size());
            List<ShopInteractor> failureInteractors = new ArrayList<>(failureInteractorInfo.size());
            List<ShopDisplay> displays = new ArrayList<>(displayInfo.size());

            ElementUtils.createElements(predicateInfo, "predicates", shopContext, predicates, "shop predicate",
                    dependencyProvider, LOGGER);
            ElementUtils.createElements(successInteractorInfo, "successInteractors", shopContext, successInteractors,
                    "shop success interactor", dependencyProvider, LOGGER);
            ElementUtils.createElements(failureInteractorInfo, "failureInteractors", shopContext, failureInteractors,
                    "shop failure interactor", dependencyProvider, LOGGER);
            ElementUtils.createElements(displayInfo, "displays", shopContext, displays, "shop display",
                    dependencyProvider, LOGGER);

            shops.add(new Shop(shopInfo, instance, predicates, successInteractors, failureInteractors, displays));
        }

        return shops;
    }

    private List<Door> buildDoors(Point mapOrigin, List<DoorInfo> doorInfoList, DependencyProvider dependencyProvider,
            Instance instance) {
        List<Door> doors = new ArrayList<>(doorInfoList.size());
        for (DoorInfo doorInfo : doorInfoList) {
            ConfigList openActionInfo = doorInfo.openActions();

            List<Action<Door>> openActions = new ArrayList<>(openActionInfo.size());

            ElementUtils.createElements(contextManager, openActionInfo, openActions, "door open action",
                    dependencyProvider, LOGGER);

            doors.add(new Door(mapOrigin, doorInfo, instance, Block.AIR, openActions));
        }

        return doors;
    }

    private List<Room> buildRooms(Point mapOrigin, List<RoomInfo> roomInfoList, DependencyProvider dependencyProvider) {
        List<Room> rooms = new ArrayList<>(roomInfoList.size());
        for (RoomInfo roomInfo : roomInfoList) {
            ConfigList openActionInfo = roomInfo.openActions();

            List<Action<Room>> openActions = new ArrayList<>(openActionInfo.size());

            ElementUtils.createElements(contextManager, openActionInfo, openActions, "room open action",
                    dependencyProvider, LOGGER);

            rooms.add(new Room(mapOrigin, roomInfo, openActions));
        }

        return rooms;
    }

    private List<Round> buildRounds(List<RoundInfo> roundInfoList, List<Spawnpoint> spawnpoints,
            DependencyProvider dependencyProvider, SpawnDistributor spawnDistributor) {
        List<Round> rounds = new ArrayList<>(roundInfoList.size());
        for (RoundInfo roundInfo : roundInfoList) {
            List<WaveInfo> waveInfo = roundInfo.waves();
            ConfigList startActionInfo = roundInfo.startActions();
            ConfigList endActionInfo = roundInfo.endActions();

            List<Wave> waves = new ArrayList<>(waveInfo.size());
            List<Action<Round>> startActions = new ArrayList<>(startActionInfo.size());
            List<Action<Round>> endActions = new ArrayList<>(endActionInfo.size());

            ElementUtils.createElements(contextManager, startActionInfo, startActions, "round start action",
                    dependencyProvider, LOGGER);
            ElementUtils.createElements(contextManager, endActionInfo, endActions, "round end action",
                    dependencyProvider, LOGGER);

            for (WaveInfo wave : roundInfo.waves()) {
                waves.add(new Wave(wave));
            }

            rounds.add(new Round(roundInfo, waves, startActions, endActions, spawnDistributor, spawnpoints));
        }

        return rounds;
    }

    public static class Module implements DependencyModule, MapObjects.Module {
        private final Instance instance;
        private final Random random;
        private final Supplier<? extends RoundHandler> roundHandlerSupplier;
        private final Flaggable flaggable;
        private final TransactionModifierSource transactionModifierSource;
        private final SlotDistributor slotDistributor;
        private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
        private final Pos respawnPos;
        private final Supplier<? extends MapObjects> mapObjectsSupplier;

        private Module(Instance instance, Random random, Supplier<? extends RoundHandler> roundHandlerSupplier,
                Flaggable flaggable, TransactionModifierSource transactionModifierSource,
                SlotDistributor slotDistributor, Map<? super UUID, ? extends ZombiesPlayer> playerMap, Pos respawnPos,
                Supplier<? extends MapObjects> mapObjectsSupplier) {
            this.instance = Objects.requireNonNull(instance, "instance");
            this.random = Objects.requireNonNull(random, "random");
            this.roundHandlerSupplier = Objects.requireNonNull(roundHandlerSupplier, "roundHandlerSupplier");
            this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
            this.transactionModifierSource = Objects.requireNonNull(transactionModifierSource, "modifierSource");
            this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
            this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
            this.respawnPos = Objects.requireNonNull(respawnPos, "respawnPos");
            this.mapObjectsSupplier = Objects.requireNonNull(mapObjectsSupplier, "mapObjectsSupplier");
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.instance")
        @Override
        public @NotNull Instance instance() {
            return instance;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.random")
        @Override
        public @NotNull Random random() {
            return random;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.round_handler_supplier")
        @Override
        public @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier() {
            return roundHandlerSupplier;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.flaggable")
        @Override
        public @NotNull Flaggable flaggable() {
            return flaggable;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.modifier_source")
        @Override
        public @NotNull TransactionModifierSource modifierSource() {
            return transactionModifierSource;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.slot_distributor")
        @Override
        public @NotNull SlotDistributor slotDistributor() {
            return slotDistributor;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.player_map")
        @Override
        public @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap() {
            return playerMap;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.player_collection")
        @Override
        public @NotNull Collection<? extends ZombiesPlayer> playerCollection() {
            return playerMap.values();
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.respawn_pos")
        @Override
        public @NotNull Pos respawnPos() {
            return respawnPos;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.map_objects")
        @Override
        public @NotNull Supplier<? extends MapObjects> mapObjectsSupplier() {
            return mapObjectsSupplier;
        }
    }
}
