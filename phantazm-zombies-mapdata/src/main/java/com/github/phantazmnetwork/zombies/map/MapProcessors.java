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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MapProcessors {
    private static final String ID = "id";
    private static final String DISPLAY_NAME = "displayName";
    private static final String DISPLAY_ITEM_TAG = "displayItemTag";
    private static final String ORIGIN = "origin";
    private static final String REGIONS = "regions";
    private static final String COST = "cost";
    private static final String OPENS_TO = "opensTo";
    private static final String TRIGGER_LOCATION = "triggerLocation";
    private static final String ROOM_NAME = "roomName";
    private static final String FRAME_REGION = "frameRegion";
    private static final String INTERNAL_REGIONS = "internalRegions";
    private static final String REPAIR_SOUND = "repairSound";
    private static final String REPAIR_ALL_SOUND = "repairAllSound";
    private static final String BREAK_SOUND = "breakSound";
    private static final String BREAK_ALL_SOUND = "breakAllSound";
    private static final String LENGTHS = "lengths";
    private static final String ROUND = "round";
    private static final String WAVES = "waves";
    private static final String SPAWNS = "spawns";
    private static final String AMOUNT = "amount";
    private static final String IS_BLACKLIST = "isBlacklist";
    private static final String POSITION = "position";
    private static final String SPAWN_RULE = "spawnRule";
    private static final String TYPE = "type";
    private static final String INTRO_MESSAGES = "introMessages";
    private static final String SCOREBOARD_HEADER = "scoreboardHeader";
    private static final String LEADERBOARD_POSITION = "leaderboardPosition";
    private static final String LEADERBOARD_LENGTH = "leaderboardLength";
    private static final String WORLD_TIME = "worldTime";
    private static final String MAX_PLAYERS = "maxPlayers";
    private static final String MIN_PLAYERS = "minPlayers";
    private static final String STARTING_COINS = "startingCoins";
    private static final String REPAIR_COINS = "repairCoins";
    private static final String WINDOW_REPAIR_RADIUS = "windowRepairRadius";
    private static final String WINDOW_REPAIR_TICKS = "windowRepairTicks";
    private static final String CORPSE_DEATH_TICKS = "corpseDeathTicks";
    private static final String REVIVE_RADIUS = "reviveRadius";
    private static final String CAN_WALLSHOOT = "canWallshoot";
    private static final String PERKS_LOST_ON_DEATH = "perksLostOnDeath";
    private static final String BASE_REVIVE_TICKS = "baseReviveTicks";
    private static final String ROLLS_PER_CHEST = "rollsPerChest";
    private static final String MILESTONE_ROUNDS = "milestoneRounds";
    private static final String DEFAULT_EQUIPMENT = "defaultEquipment";


    private static final ConfigProcessor<MapInfo> mapInfo = new ConfigProcessor<>() {
        @Override
        public MapInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = key.dataFromElement(element.getElement(ID));
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(ORIGIN));
            Component displayName = component.dataFromElement(element.getElement(DISPLAY_NAME));
            String displayItemTag = element.getStringOrThrow(DISPLAY_ITEM_TAG);
            List<Component> introMessages = componentList.dataFromElement(element.getElement(INTRO_MESSAGES));
            Component scoreboardHeader = component.dataFromElement(element.getElement(SCOREBOARD_HEADER));
            Vec3I leaderboardPosition = VectorConfigProcessors.vec3I().dataFromElement(element
                    .getElement(LEADERBOARD_POSITION));
            int leaderboardLength = element.getNumberOrThrow(LEADERBOARD_LENGTH).intValue();
            int worldTime = element.getNumberOrThrow(WORLD_TIME).intValue();
            int maxPlayers = element.getNumberOrThrow(MAX_PLAYERS).intValue();
            int minPlayers = element.getNumberOrThrow(MIN_PLAYERS).intValue();
            int startingCoins = element.getNumberOrThrow(STARTING_COINS).intValue();
            int repairCoins = element.getNumberOrThrow(REPAIR_COINS).intValue();
            double windowRepairRadius = element.getNumberOrThrow(WINDOW_REPAIR_RADIUS).doubleValue();
            int windowRepairTicks = element.getNumberOrThrow(WINDOW_REPAIR_TICKS).intValue();
            int corpseDeathTicks = element.getNumberOrThrow(CORPSE_DEATH_TICKS).intValue();
            double reviveRadius = element.getNumberOrThrow(REVIVE_RADIUS).doubleValue();
            boolean canWallshoot = element.getBooleanOrThrow(CAN_WALLSHOOT);
            boolean perksLostOnDeath = element.getBooleanOrThrow(PERKS_LOST_ON_DEATH);
            int baseReviveTicks = element.getNumberOrThrow(BASE_REVIVE_TICKS).intValue();
            int rollsPerChest = element.getNumberOrThrow(ROLLS_PER_CHEST).intValue();
            List<Integer> milestoneRounds = integerList.dataFromElement(element.getElement(MILESTONE_ROUNDS));
            List<Key> defaultEquipment = keyList.dataFromElement(element.getElement(DEFAULT_EQUIPMENT));
            return new MapInfo(id, origin, displayName, displayItemTag, introMessages, scoreboardHeader,
                    leaderboardPosition, leaderboardLength, worldTime, maxPlayers, minPlayers, startingCoins,
                    repairCoins, windowRepairRadius, windowRepairTicks, corpseDeathTicks, reviveRadius, canWallshoot,
                    perksLostOnDeath, baseReviveTicks, rollsPerChest, milestoneRounds, defaultEquipment);
        }

        @Override
        public @NotNull ConfigElement elementFromData(MapInfo mapConfig) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(23);
            node.put(ID, key.elementFromData(mapConfig.id()));
            node.put(ORIGIN, VectorConfigProcessors.vec3I().elementFromData(mapConfig.origin()));
            node.put(DISPLAY_NAME, component.elementFromData(mapConfig.displayName()));
            node.put(DISPLAY_ITEM_TAG, new ConfigPrimitive(mapConfig.displayItemTag()));
            node.put(INTRO_MESSAGES, componentList.elementFromData(mapConfig.introMessages()));
            node.put(SCOREBOARD_HEADER, component.elementFromData(mapConfig.scoreboardHeader()));
            node.put(LEADERBOARD_POSITION, VectorConfigProcessors.vec3I().elementFromData(mapConfig
                    .leaderboardPosition()));
            node.put(LEADERBOARD_LENGTH, new ConfigPrimitive(mapConfig.leaderboardLength()));
            node.put(WORLD_TIME, new ConfigPrimitive(mapConfig.worldTime()));
            node.put(MAX_PLAYERS, new ConfigPrimitive(mapConfig.maxPlayers()));
            node.put(MIN_PLAYERS, new ConfigPrimitive(mapConfig.minPlayers()));
            node.put(STARTING_COINS, new ConfigPrimitive(mapConfig.startingCoins()));
            node.put(REPAIR_COINS, new ConfigPrimitive(mapConfig.repairCoins()));
            node.put(WINDOW_REPAIR_RADIUS, new ConfigPrimitive(mapConfig.windowRepairRadius()));
            node.put(WINDOW_REPAIR_TICKS, new ConfigPrimitive(mapConfig.windowRepairTicks()));
            node.put(CORPSE_DEATH_TICKS, new ConfigPrimitive(mapConfig.corpseDeathTicks()));
            node.put(REVIVE_RADIUS, new ConfigPrimitive(mapConfig.reviveRadius()));
            node.put(CAN_WALLSHOOT, new ConfigPrimitive(mapConfig.canWallshoot()));
            node.put(PERKS_LOST_ON_DEATH, new ConfigPrimitive(mapConfig.perksLostOnDeath()));
            node.put(BASE_REVIVE_TICKS, new ConfigPrimitive(mapConfig.baseReviveTicks()));
            node.put(ROLLS_PER_CHEST, new ConfigPrimitive(mapConfig.rollsPerChest()));
            node.put(MILESTONE_ROUNDS, integerList.elementFromData(mapConfig.milestoneRounds()));
            node.put(DEFAULT_EQUIPMENT, keyList.elementFromData(mapConfig.defaultEquipment()));
            return node;
        }
    };

    private static final ConfigProcessor<RoomInfo> roomInfo = new ConfigProcessor<>() {
        @Override
        public RoomInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = key.dataFromElement(element.getElement(ID));
            Component displayName = MiniMessage.miniMessage().deserialize(element.getStringOrThrow(DISPLAY_NAME));
            List<RegionInfo> regions = regionInfoList.dataFromElement(element.getListOrThrow(REGIONS));
            return new RoomInfo(id, displayName, regions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoomInfo roomInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
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
            ConfigNode node = new LinkedConfigNode(3);
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
            ConfigNode node = new LinkedConfigNode(2);
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
            Key repairSound = key.dataFromElement(element.getElement(REPAIR_SOUND));
            Key repairAllSound = key.dataFromElement(element.getElement(REPAIR_ALL_SOUND));
            Key breakSound = key.dataFromElement(element.getElement(BREAK_SOUND));
            Key breakAllSound = key.dataFromElement(element.getElement(BREAK_ALL_SOUND));
            return new WindowInfo(room, frameRegion, internalRegions, repairSound, repairAllSound, breakSound,
                    breakAllSound);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull WindowInfo windowData) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(9);
            node.put(ROOM_NAME, key.elementFromData(windowData.room()));
            node.put(FRAME_REGION, regionInfo.elementFromData(windowData.frameRegion()));
            node.put(INTERNAL_REGIONS, regionInfoList.elementFromData(windowData.internalRegions()));
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
            ConfigNode node = new LinkedConfigNode(2);
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
            ConfigNode node = new LinkedConfigNode(2);
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
            ConfigNode node = new LinkedConfigNode(1);
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
            ConfigNode node = new LinkedConfigNode(2);
            node.put(ID, key.elementFromData(spawnInfo.id()));
            node.put(AMOUNT, new ConfigPrimitive(spawnInfo.amount()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnpointInfo> spawnpointInfo = new ConfigProcessor<>() {
        @Override
        public SpawnpointInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I position = VectorConfigProcessors.vec3I().dataFromElement(element.getElement(POSITION));
            Key spawnRule = key.dataFromElement(element.getElement(SPAWN_RULE));
            SpawnType spawnType = MapProcessors.spawnType.dataFromElement(element.getElement(TYPE));
            return new SpawnpointInfo(position, spawnRule, spawnType);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnpointInfo spawnpointInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put(POSITION, VectorConfigProcessors.vec3I().elementFromData(spawnpointInfo.position()));
            node.put(SPAWN_RULE, key.elementFromData(spawnpointInfo.spawnRule()));
            node.put(TYPE, spawnType.elementFromData(spawnpointInfo.type()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnruleInfo> spawnruleInfo = new ConfigProcessor<>() {
        @Override
        public SpawnruleInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = key.dataFromElement(element.getElement(ID));
            List<Key> spawns = keyList.dataFromElement(element.getElement(SPAWNS));
            boolean isBlacklist = element.getBooleanOrThrow(IS_BLACKLIST);
            return new SpawnruleInfo(id, spawns, isBlacklist);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnruleInfo spawnruleInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put(ID, key.elementFromData(spawnruleInfo.id()));
            node.put(SPAWNS, keyList.elementFromData(spawnruleInfo.spawns()));
            node.put(IS_BLACKLIST, new ConfigPrimitive(spawnruleInfo.isBlacklist()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnType> spawnType = new ConfigProcessor<>() {
        @Override
        public SpawnType dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if(!element.isString()) {
                throw new ConfigProcessException("Element is not a string");
            }

            try {
                return SpawnType.valueOf(element.asString());
            }
            catch (IllegalArgumentException e) {
                throw new ConfigProcessException(e);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnType spawnType) {
            return new ConfigPrimitive(spawnType.toString());
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

    private static final ConfigProcessor<Component> component = new ConfigProcessor<>() {
        @Override
        public Component dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if(!element.isString()) {
                throw new ConfigProcessException("Element is not a string");
            }

            return MiniMessage.miniMessage().deserialize(element.asString());
        }

        @Override
        public @NotNull ConfigElement elementFromData(Component component) throws ConfigProcessException {
            return new ConfigPrimitive(MiniMessage.miniMessage().serialize(component));
        }
    };

    private static final ConfigProcessor<List<Key>> keyList = ConfigProcessorUtils.newListProcessor(key);

    private static final ConfigProcessor<List<Component>> componentList = ConfigProcessorUtils
            .newListProcessor(component);

    private static final ConfigProcessor<List<RegionInfo>> regionInfoList = ConfigProcessorUtils
            .newListProcessor(regionInfo);

    private static final ConfigProcessor<List<WaveInfo>> waveInfoList = ConfigProcessorUtils.newListProcessor(waveInfo);

    private static final ConfigProcessor<List<SpawnInfo>> spawnInfoList = ConfigProcessorUtils
            .newListProcessor(spawnInfo);

    private static final ConfigProcessor<List<Integer>> integerList = ConfigProcessorUtils.newListProcessor(
            new ConfigProcessor<Integer>() {
        @Override
        public Integer dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            try {
                return element.asNumber().intValue();
            }
            catch (IllegalStateException e) {
                throw new ConfigProcessException(e);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(Integer integer) throws ConfigProcessException {
            return new ConfigPrimitive(integer);
        }
    });

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

    public static @NotNull ConfigProcessor<SpawnpointInfo> spawnpointInfo() {
        return spawnpointInfo;
    }

    public static @NotNull ConfigProcessor<SpawnruleInfo> spawnruleInfo() {
        return spawnruleInfo;
    }

    public static @NotNull ConfigProcessor<SpawnType> spawnType() {
        return spawnType;
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