package com.github.phantazmnetwork.zombies.map.objects;

import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.gui.SlotDistributor;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.spawn.SpawnDistributor;
import com.github.phantazmnetwork.zombies.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.map.action.Action;
import com.github.phantazmnetwork.zombies.map.shop.Shop;
import com.github.phantazmnetwork.zombies.map.shop.display.ShopDisplay;
import com.github.phantazmnetwork.zombies.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.phantazmnetwork.zombies.util.ElementUtils;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicMapObjectBuilder implements MapObjectBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMapObjectBuilder.class);

    private final ContextManager contextManager;
    private final Instance instance;
    private final MobStore mobStore;
    private final MobSpawner mobSpawner;
    private final ClientBlockHandler clientBlockHandler;
    private final SpawnDistributor spawnDistributor;
    private final Function<? super List<Round>, ? extends RoundHandler> roundHandlerFunction;
    private final Flaggable flaggable;
    private final ModifierSource modifierSource;
    private final SlotDistributor slotDistributor;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final Pos respawnPos;
    private final KeyParser keyParser;

    public static class Module implements DependencyModule {
        private final Instance instance;
        private final Supplier<? extends RoundHandler> roundHandlerSupplier;
        private final Flaggable flaggable;
        private final ModifierSource modifierSource;
        private final SlotDistributor slotDistributor;
        private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
        private final Pos respawnPos;
        private final Supplier<? extends MapObjects> mapObjectsSupplier;

        private Module(Instance instance, Supplier<? extends RoundHandler> roundHandlerSupplier, Flaggable flaggable,
                ModifierSource modifierSource, SlotDistributor slotDistributor,
                Map<? super UUID, ? extends ZombiesPlayer> playerMap, Pos respawnPos,
                Supplier<? extends MapObjects> mapObjectsSupplier) {
            this.instance = Objects.requireNonNull(instance, "instance");
            this.roundHandlerSupplier = Objects.requireNonNull(roundHandlerSupplier, "roundHandlerSupplier");
            this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
            this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
            this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
            this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
            this.respawnPos = Objects.requireNonNull(respawnPos, "respawnPos");
            this.mapObjectsSupplier = Objects.requireNonNull(mapObjectsSupplier, "mapObjectsSupplier");
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.instance")
        public @NotNull Instance instance() {
            return instance;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.round_handler_supplier")
        public @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier() {
            return roundHandlerSupplier;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.flaggable")
        public @NotNull Flaggable flaggable() {
            return flaggable;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.modifier_source")
        public @NotNull ModifierSource modifierSource() {
            return modifierSource;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.slot_distributor")
        public @NotNull SlotDistributor slotDistributor() {
            return slotDistributor;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.player_map")
        public @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap() {
            return playerMap;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.player_collection")
        public @NotNull Collection<? extends ZombiesPlayer> playerCollection() {
            return playerMap.values();
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.respawn_pos")
        public @NotNull Pos respawnPos() {
            return respawnPos;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.map_objects")
        public @NotNull Supplier<? extends MapObjects> mapObjectsSupplier() {
            return mapObjectsSupplier;
        }
    }

    public BasicMapObjectBuilder(@NotNull ContextManager contextManager, @NotNull Instance instance,
            @NotNull MobStore mobStore, @NotNull MobSpawner mobSpawner, @NotNull ClientBlockHandler clientBlockHandler,
            @NotNull SpawnDistributor spawnDistributor,
            @NotNull Function<? super List<Round>, ? extends RoundHandler> roundHandlerFunction,
            @NotNull Flaggable flaggable, @NotNull ModifierSource modifierSource,
            @NotNull SlotDistributor slotDistributor, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull Pos respawnPos, @NotNull KeyParser keyParser) {
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.clientBlockHandler = Objects.requireNonNull(clientBlockHandler, "clientBlockHandler");
        this.spawnDistributor = Objects.requireNonNull(spawnDistributor, "spawnDistributor");
        this.roundHandlerFunction = Objects.requireNonNull(roundHandlerFunction, "roundHandlerFunction");
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
        this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.respawnPos = Objects.requireNonNull(respawnPos);
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull MapObjects build(@NotNull MapInfo mapInfo) {
        Wrapper<MapObjects> mapObjectsWrapper = Wrapper.ofNull();
        Wrapper<RoundHandler> roundHandlerWrapper = Wrapper.ofNull();
        DependencyProvider provider = new ModuleDependencyProvider(keyParser,
                new Module(instance, roundHandlerWrapper, flaggable, modifierSource, slotDistributor, playerMap,
                        respawnPos, mapObjectsWrapper));

        Map<Key, SpawnruleInfo> spawnruleInfoMap = buildSpawnrules(mapInfo.spawnrules());
        List<Spawnpoint> spawnpoints =
                buildSpawnpoints(mapInfo.settings().origin(), mapInfo.spawnpoints(), spawnruleInfoMap);
        List<Window> windows = buildWindows(mapInfo.settings().origin(), mapInfo.windows(), provider);
        List<Shop> shops = buildShops(mapInfo.shops(), provider);
        List<Door> doors = buildDoors(mapInfo.settings().origin(), mapInfo.doors(), provider);
        List<Room> rooms = buildRooms(mapInfo.settings().origin(), mapInfo.rooms(), provider);
        List<Round> rounds = buildRounds(mapInfo.rounds(), spawnpoints, provider);

        MapObjects mapObjects = new BasicMapObjects(spawnpoints, windows, shops, doors, rooms, rounds, provider);
        mapObjectsWrapper.set(mapObjects);
        roundHandlerWrapper.set(
                Objects.requireNonNull(roundHandlerFunction.apply(rounds), "roundHandlerFunction result"));

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

    private List<Spawnpoint> buildSpawnpoints(Vec3I mapOrigin, List<SpawnpointInfo> spawnpointInfoList,
            Map<Key, SpawnruleInfo> spawnruleInfoMap) {
        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointInfoList.size());
        for (SpawnpointInfo spawnpointInfo : spawnpointInfoList) {
            spawnpoints.add(
                    new Spawnpoint(mapOrigin, spawnpointInfo, instance, spawnruleInfoMap::get, mobStore, mobSpawner));
        }

        return spawnpoints;
    }

    private List<Window> buildWindows(Vec3I mapOrigin, List<WindowInfo> windowInfoList,
            DependencyProvider dependencyProvider) {
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

    private List<Shop> buildShops(List<ShopInfo> shopInfoList, DependencyProvider dependencyProvider) {
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

    private List<Door> buildDoors(Vec3I mapOrigin, List<DoorInfo> doorInfoList, DependencyProvider dependencyProvider) {
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

    private List<Room> buildRooms(Vec3I mapOrigin, List<RoomInfo> roomInfoList, DependencyProvider dependencyProvider) {
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
            DependencyProvider dependencyProvider) {
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
}
