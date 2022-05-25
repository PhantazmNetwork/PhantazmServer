package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.ConfigProcessorUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.VectorConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MapProcessors {
    private static final String ID = "id";
    private static final String DISPLAY_NAME = "displayName";
    private static final String DISPLAY_ITEM = "displayItem";
    private static final String ORIGIN = "origin";
    private static final String ROOMS_PATH = "roomsPath";
    private static final String DOORS_PATH = "doorsPath";
    private static final String SHOPS_PATH = "shopsPath";
    private static final String WINDOWS_PATH = "windowsPath";
    private static final String ROUNDS_PATH = "roundsPath";
    private static final String VERSION = "version";
    private static final String REGIONS = "regions";
    private static final String COST = "cost";
    private static final String OPENS_TO = "opensTo";
    private static final String TRIGGER_LOCATION = "triggerLocation";
    private static final String ROOM_NAME = "roomName";
    private static final String FRAME_REGION = "frameRegion";
    private static final String INTERNAL_REGIONS = "internalRegions";
    private static final String SPAWN = "spawn";
    private static final String TARGET = "target";
    private static final String REPAIR_SOUND = "repairSound";
    private static final String REPAIR_ALL_SOUND = "repairAllSound";
    private static final String BREAK_SOUND = "breakSound";
    private static final String BREAK_ALL_SOUND = "breakAllSound";
    private static final String LENGTHS = "lengths";
    private static final String ROUND = "round";
    private static final String WAVES = "waves";
    private static final String SPAWNS = "spawns";
    private static final String AMOUNT = "amount";

    private static final ConfigProcessor<MapInfo> mapInfo = new ConfigProcessor<>() {
        private static final String DEFAULT_ROOMS_PATH = "rooms";
        private static final String DEFAULT_DOORS_PATH = "doors";
        private static final String DEFAULT_SHOPS_PATH = "shops";
        private static final String DEFAULT_WINDOWS_PATH = "windows";
        private static final String DEFAULT_ROUNDS_PATH = "rounds";

        @Override
        public MapInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int version = element.getNumberOrThrow(VERSION).intValue();
            Key id = key.dataFromElement(element.getElement(ID));
            String displayName = element.getStringOrThrow(DISPLAY_NAME);
            String displayItem = element.getStringOrThrow(DISPLAY_ITEM);
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(ORIGIN));
            String roomsPath = element.getStringOrDefault(DEFAULT_ROOMS_PATH, ROOMS_PATH);
            String doorsPath = element.getStringOrDefault(DEFAULT_DOORS_PATH, DOORS_PATH);
            String shopsPath = element.getStringOrDefault(DEFAULT_SHOPS_PATH, SHOPS_PATH);
            String windowsPath = element.getStringOrDefault(DEFAULT_WINDOWS_PATH, WINDOWS_PATH);
            String roundsPath = element.getStringOrDefault(DEFAULT_ROUNDS_PATH, ROUNDS_PATH);
            return new MapInfo(version, id, displayName, displayItem, origin, roomsPath, doorsPath, shopsPath,
                    windowsPath, roundsPath);
        }

        @Override
        public @NotNull ConfigElement elementFromData(MapInfo mapConfig) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(VERSION, new ConfigPrimitive(mapConfig.version()));
            node.put(ID, key.elementFromData(mapConfig.id()));
            node.put(DISPLAY_NAME, new ConfigPrimitive(mapConfig.displayName()));
            node.put(DISPLAY_ITEM, new ConfigPrimitive(mapConfig.displayItem()));
            node.put(ORIGIN, VectorConfigProcessors.vec3I().elementFromData(mapConfig.origin()));
            putIfNotDefault(node, ROOMS_PATH, mapConfig.roomsPath(), DEFAULT_ROOMS_PATH);
            putIfNotDefault(node, DOORS_PATH, mapConfig.doorsPath(), DEFAULT_DOORS_PATH);
            putIfNotDefault(node, SHOPS_PATH, mapConfig.shopsPath(), DEFAULT_SHOPS_PATH);
            putIfNotDefault(node, WINDOWS_PATH, mapConfig.windowsPath(), DEFAULT_WINDOWS_PATH);
            putIfNotDefault(node, ROUNDS_PATH, mapConfig.roundsPath(), DEFAULT_ROUNDS_PATH);
            return node;
        }

        private static void putIfNotDefault(ConfigNode node, String key, String path, String defaultPath) {
            if(!path.equals(defaultPath)) {
                node.put(key, new ConfigPrimitive(path));
            }
        }
    };

    private static final ConfigProcessor<RoomInfo> roomInfo = new ConfigProcessor<>() {
        @Override
        public RoomInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = key.dataFromElement(element.getElement(ID));
            String displayName = element.getStringOrThrow(DISPLAY_NAME);
            List<RegionInfo> regions = regionInfoList.dataFromElement(element.getListOrThrow(REGIONS));
            return new RoomInfo(id, displayName, regions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoomInfo roomInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ID, key.elementFromData(roomInfo.id()));
            node.put(DISPLAY_NAME, new ConfigPrimitive(roomInfo.displayName()));
            node.put(REGIONS, regionInfoList.elementFromData(roomInfo.regions()));
            return node;
        }
    };

    private static final ConfigProcessor<DoorInfo> doorInfo = new ConfigProcessor<>() {
        @Override
        public DoorInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int cost = element.getNumberOrThrow(COST).intValue();
            List<Key> opensTo = keyList.dataFromElement(element.getElement(OPENS_TO));
            List<RegionInfo> regions = regionInfoList.dataFromElement(element.getListOrThrow(REGIONS));
            return new DoorInfo(cost, opensTo, regions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(DoorInfo doorInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(COST, new ConfigPrimitive(doorInfo.cost()));
            node.put(OPENS_TO, keyList.elementFromData(doorInfo.opensTo()));
            node.put(REGIONS, regionInfoList.elementFromData(doorInfo.doorRegions()));
            return node;
        }
    };

    private static final ConfigProcessor<ShopInfo> shopInfo = new ConfigProcessor<>() {
        @Override
        public ShopInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = key.dataFromElement(element.getElement(ID));
            Vec3I triggerLocation = VectorConfigProcessors.vec3I().dataFromElement(element
                    .getElement(TRIGGER_LOCATION));
            return new ShopInfo(id, triggerLocation);
        }

        @Override
        public @NotNull ConfigElement elementFromData(ShopInfo shopInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ID, key.elementFromData(shopInfo.id()));
            node.put(TRIGGER_LOCATION, VectorConfigProcessors.vec3I().elementFromData(shopInfo.triggerLocation()));
            return node;
        }
    };

    private static final ConfigProcessor<WindowInfo> windowInfo = new ConfigProcessor<>() {
        @Override
        public @NotNull WindowInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key room = key.dataFromElement(element.getElement(ROOM_NAME));
            RegionInfo frameRegion = regionInfo.dataFromElement(element.getElement(FRAME_REGION));
            List<RegionInfo> internalRegions = regionInfoList.dataFromElement(element.getListOrThrow(INTERNAL_REGIONS));
            Vec3I spawn = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(SPAWN));
            Vec3I target = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(TARGET));
            Key repairSound = key.dataFromElement(element.getElement(REPAIR_SOUND));
            Key repairAllSound = key.dataFromElement(element.getElement(REPAIR_ALL_SOUND));
            Key breakSound = key.dataFromElement(element.getElement(BREAK_SOUND));
            Key breakAllSound = key.dataFromElement(element.getElement(BREAK_ALL_SOUND));
            return new WindowInfo(room, frameRegion, internalRegions, spawn, target, repairSound, repairAllSound,
                    breakSound, breakAllSound);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull WindowInfo windowData) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ROOM_NAME, key.elementFromData(windowData.room()));
            node.put(FRAME_REGION, regionInfo.elementFromData(windowData.frameRegion()));
            node.put(INTERNAL_REGIONS, regionInfoList.elementFromData(windowData.internalRegions()));
            node.put(SPAWN, VectorConfigProcessors.vec3I().elementFromData(windowData.spawn()));
            node.put(TARGET, VectorConfigProcessors.vec3I().elementFromData(windowData.target()));
            node.put(REPAIR_SOUND, key.elementFromData(windowData.repairSound()));
            node.put(REPAIR_ALL_SOUND, key.elementFromData(windowData.repairAllSound()));
            node.put(BREAK_SOUND, key.elementFromData(windowData.breakSound()));
            node.put(BREAK_ALL_SOUND, key.elementFromData(windowData.breakAllSound()));
            return node;
        }
    };

    private static final ConfigProcessor<RegionInfo> regionInfo = new ConfigProcessor<>() {
        @Override
        public RegionInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(ORIGIN));
            Vec3I lengths = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(LENGTHS));
            return new RegionInfo(origin, lengths);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RegionInfo regionInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ORIGIN, VectorConfigProcessors.vec3I().elementFromData(regionInfo.origin()));
            node.put(LENGTHS, VectorConfigProcessors.vec3I().elementFromData(regionInfo.lengths()));
            return node;
        }
    };

    private static final ConfigProcessor<RoundInfo> roundInfo = new ConfigProcessor<>() {
        @Override
        public RoundInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int round = element.getNumberOrThrow(ROUND).intValue();
            List<WaveInfo> waves = waveInfoList.dataFromElement(element.getListOrThrow(WAVES));
            return new RoundInfo(round, waves);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoundInfo roundInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ROUND, new ConfigPrimitive(roundInfo.round()));
            node.put(WAVES, waveInfoList.elementFromData(roundInfo.waves()));
            return node;
        }
    };

    private static final ConfigProcessor<WaveInfo> waveInfo = new ConfigProcessor<>() {
        @Override
        public WaveInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            List<SpawnInfo> spawns = spawnInfoList.dataFromElement(element.getListOrThrow(SPAWNS));
            return new WaveInfo(spawns);
        }

        @Override
        public @NotNull ConfigElement elementFromData(WaveInfo waveInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(SPAWNS, spawnInfoList.elementFromData(waveInfo.spawns()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnInfo> spawnInfo = new ConfigProcessor<>() {
        @Override
        public SpawnInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = key.dataFromElement(element.getElement(ID));
            int amount = element.getNumberOrThrow(AMOUNT).intValue();
            return new SpawnInfo(id, amount);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnInfo spawnInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ID, key.elementFromData(spawnInfo.id()));
            node.put(AMOUNT, new ConfigPrimitive(spawnInfo.amount()));
            return node;
        }
    };

    private static final ConfigProcessor<Key> key = new ConfigProcessor<>() {
        @Override
        public Key dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if(!element.isString()) {
                throw new ConfigProcessException("Element must be a string");
            }

            try {
                //noinspection PatternValidation
                return Key.key(element.asString());
            }
            catch (InvalidKeyException keyException) {
                throw new ConfigProcessException(keyException);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(Key key) {
            return new ConfigPrimitive(key.asString());
        }
    };

    private static final ConfigProcessor<List<Key>> keyList = ConfigProcessorUtils.newListProcessor(key);

    private static final ConfigProcessor<List<RegionInfo>> regionInfoList = ConfigProcessorUtils
            .newListProcessor(regionInfo);

    private static final ConfigProcessor<List<WaveInfo>> waveInfoList = ConfigProcessorUtils.newListProcessor(waveInfo);

    private static final ConfigProcessor<List<SpawnInfo>> spawnInfoList = ConfigProcessorUtils
            .newListProcessor(spawnInfo);

    private MapProcessors() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull ConfigProcessor<MapInfo> mapInfo() {
        return mapInfo;
    }

    public static @NotNull ConfigProcessor<RoomInfo> roomInfo() {
        return roomInfo;
    }

    public static @NotNull ConfigProcessor<DoorInfo> doorInfo() {
        return doorInfo;
    }

    public static @NotNull ConfigProcessor<ShopInfo> shopInfo() { return shopInfo; }

    public static @NotNull ConfigProcessor<WindowInfo> windowInfo() {
        return windowInfo;
    }

    public static @NotNull ConfigProcessor<RegionInfo> regionInfo() {
        return regionInfo;
    }

    public static @NotNull ConfigProcessor<RoundInfo> roundInfo() {
        return roundInfo;
    }

    public static @NotNull ConfigProcessor<WaveInfo> waveInfo() {
        return waveInfo;
    }

    public static @NotNull ConfigProcessor<SpawnInfo> spawnInfo() {
        return spawnInfo;
    }
}