package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.api.MinestomConfigProcessors;
import com.github.phantazmnetwork.commons.ConfigProcessorUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.VectorConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MapProcessors {
    private static final ConfigProcessor<MapInfo> mapInfo = new ConfigProcessor<>() {
        private static final String NAME_STRING = "name";
        private static final String DISPLAY_NAME_STRING = "displayName";
        private static final String DISPLAY_ITEM_STRING = "displayItem";
        private static final String ORIGIN_STRING = "origin";
        private static final String ROOMS_PATH_STRING = "roomsPath";
        private static final String WINDOWS_PATH_STRING = "windowsPath";
        private static final String ROUNDS_PATH_STRING = "roundsPath";
        private static final String VERSION_STRING = "version";

        @Override
        public MapInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String name = element.getStringOrThrow(NAME_STRING);
            String displayName = element.getStringOrThrow(DISPLAY_NAME_STRING);
            ItemStack displayItem = MinestomConfigProcessors.itemStack().dataFromElement(element.getElement(
                    DISPLAY_ITEM_STRING));
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(ORIGIN_STRING));
            String roomsPath = element.getStringOrThrow(ROOMS_PATH_STRING);
            String windowsPath = element.getStringOrThrow(WINDOWS_PATH_STRING);
            String roundsPath = element.getStringOrThrow(ROUNDS_PATH_STRING);
            int version = element.getNumberOrThrow(VERSION_STRING).intValue();
            return new MapInfo(name, displayName, displayItem, origin, roomsPath, windowsPath, roundsPath, version);
        }

        @Override
        public @NotNull ConfigElement elementFromData(MapInfo mapConfig) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(NAME_STRING, new ConfigPrimitive(mapConfig.name()));
            node.put(DISPLAY_NAME_STRING, new ConfigPrimitive(mapConfig.displayName()));
            node.put(DISPLAY_ITEM_STRING, MinestomConfigProcessors.itemStack().elementFromData(mapConfig
                    .displayItem()));
            node.put(ORIGIN_STRING, VectorConfigProcessors.vec3I().elementFromData(mapConfig.origin()));
            node.put(ROOMS_PATH_STRING, new ConfigPrimitive(mapConfig.roomsPath()));
            node.put(WINDOWS_PATH_STRING, new ConfigPrimitive(mapConfig.windowsPath()));
            node.put(ROUNDS_PATH_STRING, new ConfigPrimitive(mapConfig.roundsPath()));
            node.put(VERSION_STRING, new ConfigPrimitive(mapConfig.version()));
            return node;
        }
    };

    private static final ConfigProcessor<RoomInfo> roomInfo = new ConfigProcessor<>() {
        private static final String NAME_STRING = "name";
        private static final String DISPLAY_NAME_STRING = "displayName";
        private static final String REGIONS_STRING = "regions";

        @Override
        public RoomInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String name = element.getStringOrThrow(NAME_STRING);
            String displayName = element.getStringOrThrow(DISPLAY_NAME_STRING);
            List<RegionInfo> regions = regionInfoList.dataFromElement(element.getListOrThrow(REGIONS_STRING));
            return new RoomInfo(name, displayName, regions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoomInfo roomInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(NAME_STRING, new ConfigPrimitive(roomInfo.name()));
            node.put(DISPLAY_NAME_STRING, new ConfigPrimitive(roomInfo.displayName()));
            node.put(REGIONS_STRING, regionInfoList.elementFromData(roomInfo.regions()));
            return node;
        }
    };

    private static final ConfigProcessor<DoorInfo> doorInfo = new ConfigProcessor<>() {
        private static final String COST_STRING = "cost";
        private static final String OPENS_TO_STRING = "opensTo";
        private static final String REGIONS_STRING = "regions";

        @Override
        public DoorInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int cost = element.getNumberOrThrow(COST_STRING).intValue();
            List<String> opensTo = stringList.dataFromElement(element.getListOrThrow(OPENS_TO_STRING));
            List<RegionInfo> regions = regionInfoList.dataFromElement(element.getListOrThrow(REGIONS_STRING));
            return new DoorInfo(cost, opensTo, regions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(DoorInfo doorInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(COST_STRING, new ConfigPrimitive(doorInfo.cost()));
            node.put(OPENS_TO_STRING, stringList.elementFromData(doorInfo.opensTo()));
            node.put(REGIONS_STRING, regionInfoList.elementFromData(doorInfo.doorRegions()));
            return node;
        }
    };

    private static final ConfigProcessor<WindowInfo> windowInfo = new ConfigProcessor<>() {
        private static final String ROOM_NAME_STRING = "roomName";
        private static final String FRAME_REGION_STRING = "frameRegion";
        private static final String INTERNAL_REGIONS_STRING = "internalRegions";
        private static final String SPAWN_STRING = "spawn";
        private static final String TARGET_STRING = "target";

        @Override
        public @NotNull WindowInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String roomName = element.getStringOrThrow(ROOM_NAME_STRING);
            RegionInfo frameRegion = regionInfo.dataFromElement(element.getElement(FRAME_REGION_STRING));
            List<RegionInfo> internalRegions = regionInfoList.dataFromElement(element.getListOrThrow(
                    INTERNAL_REGIONS_STRING));
            Vec3I spawn = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(SPAWN_STRING));
            Vec3I target = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(TARGET_STRING));
            return new WindowInfo(roomName, frameRegion, internalRegions, spawn, target);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull WindowInfo windowData) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ROOM_NAME_STRING, new ConfigPrimitive(windowData.roomName()));
            node.put(FRAME_REGION_STRING, regionInfo.elementFromData(windowData.frameRegion()));
            node.put(INTERNAL_REGIONS_STRING, regionInfoList.elementFromData(windowData.internalRegions()));
            node.put(SPAWN_STRING, VectorConfigProcessors.vec3I().elementFromData(windowData.spawn()));
            node.put(TARGET_STRING, VectorConfigProcessors.vec3I().elementFromData(windowData.target()));
            return node;
        }
    };

    private static final ConfigProcessor<RegionInfo> regionInfo = new ConfigProcessor<>() {
        private static final String ORIGIN_STRING = "origin";
        private static final String LENGTHS_STRING = "lengths";

        @Override
        public RegionInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(ORIGIN_STRING));
            Vec3I lengths = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(LENGTHS_STRING));

            return new RegionInfo(origin, lengths);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RegionInfo regionInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ORIGIN_STRING, VectorConfigProcessors.vec3I().elementFromData(regionInfo.origin()));
            node.put(LENGTHS_STRING, VectorConfigProcessors.vec3I().elementFromData(regionInfo.lengths()));
            return node;
        }
    };

    private static final ConfigProcessor<RoundInfo> roundInfo = new ConfigProcessor<>() {
        private static final String ROUND_STRING = "round";
        private static final String WAVES_STRING = "waves";

        @Override
        public RoundInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int round = element.getNumberOrThrow(ROUND_STRING).intValue();
            List<WaveInfo> waves = waveInfoList.dataFromElement(element.getListOrThrow(WAVES_STRING));
            return new RoundInfo(round, waves);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoundInfo roundInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(ROUND_STRING, new ConfigPrimitive(roundInfo.round()));
            node.put(WAVES_STRING, waveInfoList.elementFromData(roundInfo.waves()));
            return node;
        }
    };

    private static final ConfigProcessor<WaveInfo> waveInfo = new ConfigProcessor<>() {
        private static final String WAVES_STRING = "spawns";

        @Override
        public WaveInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            List<SpawnInfo> spawns = spawnInfoList.dataFromElement(element.getListOrThrow(WAVES_STRING));
            return new WaveInfo(spawns);
        }

        @Override
        public @NotNull ConfigElement elementFromData(WaveInfo waveInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode();
            node.put(WAVES_STRING, spawnInfoList.elementFromData(waveInfo.spawns()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnInfo> spawnInfo = new ConfigProcessor<>() {
        private static final String TYPE_STRING = "type";
        private static final String AMOUNT_STRING = "amount";

        @Override
        public SpawnInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String type = element.getStringOrThrow(TYPE_STRING);
            int amount = element.getNumberOrThrow(AMOUNT_STRING).intValue();
            return new SpawnInfo(type, amount);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnInfo spawnInfo) {
            ConfigNode node = new LinkedConfigNode();
            node.put(TYPE_STRING, new ConfigPrimitive(spawnInfo.type()));
            node.put(AMOUNT_STRING, new ConfigPrimitive(spawnInfo.amount()));
            return node;
        }
    };

    private static final ConfigProcessor<List<String>> stringList = ConfigProcessorUtils.newListProcessor(
            new ConfigProcessor<>() {
        @Override
        public String dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if(!element.isString()) {
                throw new ConfigProcessException("The given element is not a string");
            }

            return element.asString();
        }

        @Override
        public @NotNull ConfigElement elementFromData(String s) {
            return new ConfigPrimitive(s);
        }
    });

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
