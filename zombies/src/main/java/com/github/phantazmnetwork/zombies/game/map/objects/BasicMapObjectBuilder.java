package com.github.phantazmnetwork.zombies.game.map.objects;

import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.gui.SlotDistributor;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.map.*;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.game.map.shop.display.ShopDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.map.*;
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
    private final MobSpawner mobSpawner;
    private final ClientBlockHandler clientBlockHandler;
    private final SpawnDistributor spawnDistributor;

    private final DependencyProvider dependencyProvider;

    public static class Module implements DependencyModule {
        private final Instance instance;
        private final RoundHandler roundHandler;
        private final Flaggable flaggable;
        private final ModifierSource modifierSource;
        private final SlotDistributor slotDistributor;
        private final Function<? super UUID, ? extends ZombiesPlayer> playerFunction;
        private final EquipmentCreator equipmentCreator;

        private Module(Instance instance, RoundHandler roundHandler, Flaggable flaggable, ModifierSource modifierSource,
                SlotDistributor slotDistributor, Function<? super UUID, ? extends ZombiesPlayer> playerFunction,
                EquipmentCreator equipmentCreator) {
            this.instance = Objects.requireNonNull(instance, "instance");
            this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
            this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
            this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
            this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
            this.playerFunction = Objects.requireNonNull(playerFunction, "playerFunction");
            this.equipmentCreator = Objects.requireNonNull(equipmentCreator, "equipmentCreator");
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.instance")
        public Instance instance() {
            return instance;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.round_handler")
        public RoundHandler roundHandler() {
            return roundHandler;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.flaggable")
        public Flaggable flaggable() {
            return flaggable;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.modifier_source")
        public ModifierSource modifierSource() {
            return modifierSource;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.slot_distributor")
        public SlotDistributor slotDistributor() {
            return slotDistributor;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.player_function")
        public Function<? super UUID, ? extends ZombiesPlayer> playerFunction() {
            return playerFunction;
        }

        @Memoize
        @DependencySupplier("zombies.dependency.map_object.equipment_creator")
        public EquipmentCreator equipmentCreator() {
            return equipmentCreator;
        }
    }

    public BasicMapObjectBuilder(@NotNull ContextManager contextManager, @NotNull Instance instance,
            @NotNull MobSpawner mobSpawner, @NotNull ClientBlockHandler clientBlockHandler,
            @NotNull SpawnDistributor spawnDistributor, @NotNull RoundHandler roundHandler,
            @NotNull Flaggable flaggable, @NotNull ModifierSource modifierSource,
            @NotNull SlotDistributor slotDistributor,
            @NotNull Function<? super UUID, ? extends ZombiesPlayer> playerFunction,
            @NotNull EquipmentCreator equipmentCreator, @NotNull KeyParser keyParser) {
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.clientBlockHandler = Objects.requireNonNull(clientBlockHandler, "clientBlockHandler");
        this.spawnDistributor = Objects.requireNonNull(spawnDistributor, "spawnDistributor");

        this.dependencyProvider = new ModuleDependencyProvider(
                new Module(instance, roundHandler, flaggable, modifierSource, slotDistributor, playerFunction,
                        equipmentCreator), Objects.requireNonNull(keyParser, "keyParser"));
    }

    @Override
    public @NotNull MapObjects build(@NotNull MapInfo mapInfo) {
        Map<Key, SpawnruleInfo> spawnruleInfoMap = buildSpawnrules(mapInfo.spawnrules());
        List<Spawnpoint> spawnpoints = buildSpawnpoints(mapInfo.spawnpoints(), spawnruleInfoMap);
        List<Window> windows = buildWindows(mapInfo.windows());
        List<Shop> shops = buildShops(mapInfo.shops());
        List<Door> doors = buildDoors(mapInfo.doors());
        List<Room> rooms = buildRooms(mapInfo.rooms());
        List<Round> rounds = buildRounds(mapInfo.rounds(), spawnpoints);

        return new BasicMapObjects(spawnpoints, windows, shops, doors, rooms, rounds);
    }

    private static void createElement(ConfigElement element, Consumer<ConfigNode> action,
            Supplier<String> elementName) {
        if (element.isNode()) {
            try {
                action.accept(element.asNode());
            }
            catch (Throwable e) {
                LOGGER.warn("Exception thrown when creating element object '" + elementName.get() + "'", e);
            }
            return;
        }

        LOGGER.warn("Expected ConfigNode, was {}", element);
    }

    private static ConfigList extractList(ConfigNode rootNode, String path) {
        try {
            return rootNode.getListOrThrow(path);
        }
        catch (ConfigProcessException e) {
            LOGGER.warn("Error getting ConfigList from path '" + path + "'", e);
        }

        return ConfigList.of();
    }

    private <T> void createElements(ConfigList list, Collection<T> collection, Supplier<String> elementName) {
        for (ConfigElement element : list) {
            createElement(element, node -> collection.add(contextManager.makeContext(node).provide(dependencyProvider)),
                    elementName);
        }
    }

    private <T> void createElements(ConfigList list, String basePath, ElementContext context, Collection<T> collection,
            Supplier<String> elementName) {
        for (int i = 0; i < list.size(); i++) {
            ConfigElement element = list.get(i);

            int finalI = i;
            createElement(element, node -> collection.add(context.provide(basePath + "/" + finalI)), elementName);
        }
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

    private List<Spawnpoint> buildSpawnpoints(List<SpawnpointInfo> spawnpointInfoList,
            Map<Key, SpawnruleInfo> spawnruleInfoMap) {
        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointInfoList.size());
        for (SpawnpointInfo spawnpointInfo : spawnpointInfoList) {
            spawnpoints.add(new Spawnpoint(spawnpointInfo, instance, spawnruleInfoMap::get, mobSpawner));
        }

        return spawnpoints;
    }

    private List<Window> buildWindows(List<WindowInfo> windowInfoList) {
        List<Window> windows = new ArrayList<>(windowInfoList.size());
        for (WindowInfo windowInfo : windowInfoList) {
            ConfigList repairActionInfo = windowInfo.repairActions();
            ConfigList breakActionInfo = windowInfo.breakActions();

            List<Action<Window>> repairActions = new ArrayList<>(repairActionInfo.size());
            List<Action<Window>> breakActions = new ArrayList<>(breakActionInfo.size());

            createElements(repairActionInfo, repairActions, () -> "window repair action");
            createElements(breakActionInfo, breakActions, () -> "window break action");

            windows.add(new Window(instance, windowInfo, clientBlockHandler, repairActions, breakActions));
        }

        return windows;
    }

    private List<Shop> buildShops(List<ShopInfo> shopInfoList) {
        List<Shop> shops = new ArrayList<>(shopInfoList.size());
        for (ShopInfo shopInfo : shopInfoList) {
            ConfigNode rootNode = shopInfo.data();
            ElementContext shopContext = contextManager.makeContext(rootNode);

            ConfigList predicateInfo = extractList(rootNode, "predicates");
            ConfigList successInteractorInfo = extractList(rootNode, "successInteractors");
            ConfigList failureInteractorInfo = extractList(rootNode, "failureInteractors");
            ConfigList displayInfo = extractList(rootNode, "displays");

            List<ShopPredicate> predicates = new ArrayList<>(predicateInfo.size());
            List<ShopInteractor> successInteractors = new ArrayList<>(successInteractorInfo.size());
            List<ShopInteractor> failureInteractors = new ArrayList<>(failureInteractorInfo.size());
            List<ShopDisplay> displays = new ArrayList<>(displayInfo.size());

            createElements(predicateInfo, "predicates", shopContext, predicates, () -> "shop predicate");
            createElements(successInteractorInfo, "successInteractors", shopContext, successInteractors,
                    () -> "shop success interactor");
            createElements(failureInteractorInfo, "failureInteractors", shopContext, failureInteractors,
                    () -> "shop failure interactor");
            createElements(displayInfo, "displays", shopContext, displays, () -> "shop display");

            shops.add(new Shop(shopInfo, instance, predicates, successInteractors, failureInteractors, displays));
        }

        return shops;
    }

    private List<Door> buildDoors(List<DoorInfo> doorInfoList) {
        List<Door> doors = new ArrayList<>(doorInfoList.size());
        for (DoorInfo doorInfo : doorInfoList) {
            ConfigList openActionInfo = doorInfo.openActions();

            List<Action<Door>> openActions = new ArrayList<>(openActionInfo.size());

            createElements(openActionInfo, openActions, () -> "door open action");

            doors.add(new Door(doorInfo, instance, Block.AIR, openActions));
        }

        return doors;
    }

    private List<Room> buildRooms(List<RoomInfo> roomInfoList) {
        List<Room> rooms = new ArrayList<>(roomInfoList.size());
        for (RoomInfo roomInfo : roomInfoList) {
            ConfigList openActionInfo = roomInfo.openActions();

            List<Action<Room>> openActions = new ArrayList<>(openActionInfo.size());

            createElements(openActionInfo, openActions, () -> "room open action");

            rooms.add(new Room(roomInfo, instance, openActions));
        }

        return rooms;
    }

    private List<Round> buildRounds(List<RoundInfo> roundInfoList, List<Spawnpoint> spawnpoints) {
        List<Round> rounds = new ArrayList<>(roundInfoList.size());
        for (RoundInfo roundInfo : roundInfoList) {
            List<WaveInfo> waveInfo = roundInfo.waves();
            ConfigList startActionInfo = roundInfo.startActions();
            ConfigList endActionInfo = roundInfo.endActions();

            List<Wave> waves = new ArrayList<>(waveInfo.size());
            List<Action<Round>> startActions = new ArrayList<>(startActionInfo.size());
            List<Action<Round>> endActions = new ArrayList<>(endActionInfo.size());

            createElements(startActionInfo, startActions, () -> "round start action");
            createElements(endActionInfo, endActions, () -> "round end action");

            for (WaveInfo wave : roundInfo.waves()) {
                waves.add(new Wave(wave));
            }

            rounds.add(new Round(roundInfo, instance, waves, startActions, endActions, spawnDistributor, spawnpoints));
        }

        return rounds;
    }
}
