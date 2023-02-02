package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ConfigProcessors;
import org.phantazm.commons.vector.VectorConfigProcessors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains static {@link ConfigProcessor} instances used for serializing or deserializing various map-related data
 * objects.
 */
public final class MapProcessors {
    private static final ConfigProcessor<SpawnInfo> spawnInfo = new ConfigProcessor<>() {
        @Override
        public SpawnInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Key spawnType = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("spawnType"));
            int amount = element.getNumberOrThrow("amount").intValue();
            return new SpawnInfo(id, spawnType, amount);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnInfo spawnInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("id", ConfigProcessors.key().elementFromData(spawnInfo.id()));
            node.put("spawnType", ConfigProcessors.key().elementFromData(spawnInfo.spawnType()));
            node.putNumber("amount", spawnInfo.amount());
            return node;
        }
    };
    private static final ConfigProcessor<SpawnpointInfo> spawnpointInfo = new ConfigProcessor<>() {
        @Override
        public SpawnpointInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I position = VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("position"));
            Key spawnRule = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("spawnRule"));
            return new SpawnpointInfo(position, spawnRule);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnpointInfo spawnpointInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("position", VectorConfigProcessors.vec3I().elementFromData(spawnpointInfo.position()));
            node.put("spawnRule", ConfigProcessors.key().elementFromData(spawnpointInfo.spawnRule()));
            return node;
        }
    };

    private static final ConfigProcessor<List<Key>> keyList = ConfigProcessors.key().listProcessor();
    private static final ConfigProcessor<Set<Key>> keySet = ConfigProcessors.key().collectionProcessor(HashSet::new);
    private static final ConfigProcessor<SpawnruleInfo> spawnruleInfo = new ConfigProcessor<>() {
        @Override
        public SpawnruleInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Key spawnType = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("spawnType"));
            Set<Key> spawns = keySet.dataFromElement(element.getElementOrThrow("spawns"));
            boolean isBlacklist = element.getBooleanOrThrow("isBlacklist");
            int sla = element.getNumberOrThrow("sla").intValue();
            return new SpawnruleInfo(id, spawnType, spawns, isBlacklist, sla);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnruleInfo spawnruleInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(4);
            node.put("id", ConfigProcessors.key().elementFromData(spawnruleInfo.id()));
            node.put("spawnType", ConfigProcessors.key().elementFromData(spawnruleInfo.spawnType()));
            node.put("spawns", keySet.elementFromData(spawnruleInfo.spawns()));
            node.putBoolean("isBlacklist", spawnruleInfo.isBlacklist());
            node.putNumber("sla", spawnruleInfo.sla());
            return node;
        }
    };
    private static final ConfigProcessor<List<Component>> componentList = ConfigProcessors.component().listProcessor();
    private static final ConfigProcessor<HologramInfo> hologramInfo = new ConfigProcessor<>() {
        @Override
        public HologramInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            List<Component> text = componentList.dataFromElement(element.getElementOrThrow("text"));
            Vec3D position = VectorConfigProcessors.vec3D().dataFromElement(element.getElementOrThrow("position"));
            return new HologramInfo(text, position);
        }

        @Override
        public @NotNull ConfigElement elementFromData(HologramInfo hologramInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("text", componentList.elementFromData(hologramInfo.text()));
            node.put("position", VectorConfigProcessors.vec3D().elementFromData(hologramInfo.position()));
            return node;
        }
    };
    private static final ConfigProcessor<List<HologramInfo>> hologramInfoList = hologramInfo.listProcessor();
    private static final ConfigProcessor<DoorInfo> doorInfo = new ConfigProcessor<>() {
        @Override
        public DoorInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            List<Key> opensTo = keyList.dataFromElement(element.getElementOrThrow("opensTo"));
            List<Integer> costs = integerList.dataFromElement(element.getElementOrThrow("costs"));
            List<HologramInfo> hologramInfos = hologramInfoList.dataFromElement(element.getElementOrThrow("holograms"));
            List<Bounds3I> regions = boundsList.dataFromElement(element.getListOrThrow("regions"));
            Sound openSound = ConfigProcessors.sound().dataFromElement(element.getElementOrThrow("openSound"));
            ConfigList openActions = element.getListOrThrow("openActions");
            ConfigList closeActions = element.getListOrThrow("closeActions");
            ConfigList failOpenActions = element.getListOrThrow("failOpenActions");
            return new DoorInfo(id, opensTo, costs, hologramInfos, regions, openSound, openActions, closeActions,
                    failOpenActions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(DoorInfo doorInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(9);
            node.put("id", ConfigProcessors.key().elementFromData(doorInfo.id()));
            node.put("opensTo", keyList.elementFromData(doorInfo.opensTo()));
            node.put("costs", integerList.elementFromData(doorInfo.costs()));
            node.put("holograms", hologramInfoList.elementFromData(doorInfo.holograms()));
            node.put("regions", boundsList.elementFromData(doorInfo.regions()));
            node.put("openSound", ConfigProcessors.sound().elementFromData(doorInfo.openSound()));
            node.put("openActions", doorInfo.openActions());
            node.put("closeActions", doorInfo.closeActions());
            node.put("failOpenActions", doorInfo.failOpenActions());
            return node;
        }
    };
    private static final ConfigProcessor<List<Bounds3I>> boundsList = VectorConfigProcessors.bounds3I().listProcessor();
    private static final ConfigProcessor<RoomInfo> roomInfo = new ConfigProcessor<>() {
        @Override
        public RoomInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Component displayName =
                    ConfigProcessors.component().dataFromElement(element.getElementOrThrow("displayName"));
            List<Bounds3I> regions = boundsList.dataFromElement(element.getElementOrThrow("regions"));
            ConfigList openActions = element.getListOrThrow("openActions");
            return new RoomInfo(id, displayName, regions, openActions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoomInfo roomInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(4);
            node.put("id", ConfigProcessors.key().elementFromData(roomInfo.id()));
            node.put("displayName", ConfigProcessors.component().elementFromData(roomInfo.displayName()));
            node.put("regions", boundsList.elementFromData(roomInfo.regions()));
            node.put("openActions", roomInfo.openActions());
            return node;
        }
    };
    private static final ConfigProcessor<List<SpawnInfo>> spawnInfoList = spawnInfo.listProcessor();
    private static final ConfigProcessor<WaveInfo> waveInfo = new ConfigProcessor<>() {
        @Override
        public WaveInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            long delayTicks = element.getNumberOrThrow("delayTicks").longValue();
            List<SpawnInfo> spawns = spawnInfoList.dataFromElement(element.getListOrThrow("spawns"));
            return new WaveInfo(delayTicks, spawns);
        }

        @Override
        public @NotNull ConfigElement elementFromData(WaveInfo waveInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.putNumber("delayTicks", waveInfo.delayTicks());
            node.put("spawns", spawnInfoList.elementFromData(waveInfo.spawns()));
            return node;
        }
    };
    private static final ConfigProcessor<List<WaveInfo>> waveInfoList = waveInfo.listProcessor();
    private static final ConfigProcessor<RoundInfo> roundInfo = new ConfigProcessor<>() {
        @Override
        public RoundInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int round = element.getNumberOrThrow("round").intValue();
            ConfigList startActions = element.getListOrThrow("startActions");
            ConfigList endActions = element.getListOrThrow("endActions");
            List<WaveInfo> waves = waveInfoList.dataFromElement(element.getListOrThrow("waves"));
            return new RoundInfo(round, startActions, endActions, waves);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoundInfo roundInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(4);
            node.putNumber("round", roundInfo.round());
            node.put("startActions", roundInfo.startActions());
            node.put("endActions", roundInfo.endActions());
            node.put("waves", waveInfoList.elementFromData(roundInfo.waves()));
            return node;
        }
    };
    private static final ConfigProcessor<List<Integer>> integerList = ConfigProcessor.INTEGER.listProcessor();
    private static final ConfigProcessor<MapSettingsInfo> mapInfo = new ConfigProcessor<>() {
        @Override
        public MapSettingsInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int mapDataVersion = element.getNumberOrThrow("mapDataVersion").intValue();
            if (mapDataVersion != MapSettingsInfo.MAP_DATA_VERSION) {
                throw new ConfigProcessException("Invalid map data version " + mapDataVersion + ", expected " +
                        MapSettingsInfo.MAP_DATA_VERSION);
            }

            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            List<String> instancePath =
                    ConfigProcessor.STRING.listProcessor().dataFromElement(element.getListOrThrow("instancePath"));
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("origin"));
            Vec3I spawn = VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("spawn"));
            int minimumProtocolVersion = element.getNumberOrThrow("minimumProtocolVersion").intValue();
            int maximumProtocolVersion = element.getNumberOrThrow("maximumProtocolVersion").intValue();
            float pitch = element.getNumberOrThrow("pitch").floatValue();
            float yaw = element.getNumberOrThrow("yaw").floatValue();
            Component displayName =
                    ConfigProcessors.component().dataFromElement(element.getElementOrThrow("displayName"));
            String displayItemTag = element.getStringOrThrow("displayItemSnbt");
            List<Component> introMessages = componentList.dataFromElement(element.getElementOrThrow("introMessages"));
            Component scoreboardHeader =
                    ConfigProcessors.component().dataFromElement(element.getElementOrThrow("scoreboardHeader"));
            Vec3I leaderboardPosition =
                    VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("leaderboardPosition"));
            int leaderboardLength = element.getNumberOrThrow("leaderboardLength").intValue();
            int worldTime = element.getNumberOrThrow("worldTime").intValue();
            int maxPlayers = element.getNumberOrThrow("maxPlayers").intValue();
            int minPlayers = element.getNumberOrThrow("minPlayers").intValue();
            int startingCoins = element.getNumberOrThrow("startingCoins").intValue();
            int repairCoins = element.getNumberOrThrow("repairCoins").intValue();
            double windowRepairRadius = element.getNumberOrThrow("windowRepairRadius").doubleValue();
            double powerupPickupRadius = element.getNumberOrThrow("powerupPickupRadius").doubleValue();
            long windowRepairTicks = element.getNumberOrThrow("windowRepairTicks").longValue();
            long corpseDeathTicks = element.getNumberOrThrow("corpseDeathTicks").longValue();
            double reviveRadius = element.getNumberOrThrow("reviveRadius").doubleValue();
            boolean canWallshoot = element.getBooleanOrThrow("canWallshoot");
            boolean perksLostOnDeath = element.getBooleanOrThrow("perksLostOnDeath");
            long baseReviveTicks = element.getNumberOrThrow("baseReviveTicks").longValue();
            int rollsPerChest = element.getNumberOrThrow("rollsPerChest").intValue();
            List<Integer> milestoneRounds = integerList.dataFromElement(element.getElementOrThrow("milestoneRounds"));
            List<Key> defaultEquipment = keyList.dataFromElement(element.getElementOrThrow("defaultEquipment"));
            return new MapSettingsInfo(mapDataVersion, id, instancePath, origin, minimumProtocolVersion,
                    maximumProtocolVersion, spawn, pitch, yaw, displayName, displayItemTag, introMessages,
                    scoreboardHeader, leaderboardPosition, leaderboardLength, worldTime, maxPlayers, minPlayers,
                    startingCoins, repairCoins, windowRepairRadius, powerupPickupRadius, windowRepairTicks,
                    corpseDeathTicks, reviveRadius, canWallshoot, perksLostOnDeath, baseReviveTicks, rollsPerChest,
                    milestoneRounds, defaultEquipment);
        }

        @Override
        public @NotNull ConfigElement elementFromData(MapSettingsInfo mapConfig) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(29);
            node.putNumber("mapDataVersion", mapConfig.mapDataVersion());
            node.put("id", ConfigProcessors.key().elementFromData(mapConfig.id()));
            node.put("instancePath", ConfigProcessor.STRING.listProcessor().elementFromData(mapConfig.instancePath()));
            node.put("origin", VectorConfigProcessors.vec3I().elementFromData(mapConfig.origin()));
            node.putNumber("minimumProtocolVersion", mapConfig.minimumProtocolVersion());
            node.putNumber("maximumProtocolVersion", mapConfig.maximumProtocolVersion());
            node.put("spawn", VectorConfigProcessors.vec3I().elementFromData(mapConfig.spawn()));
            node.putNumber("pitch", mapConfig.pitch());
            node.putNumber("yaw", mapConfig.yaw());
            node.put("displayName", ConfigProcessors.component().elementFromData(mapConfig.displayName()));
            node.putString("displayItemSnbt", mapConfig.displayItemSnbt());
            node.put("introMessages", componentList.elementFromData(mapConfig.introMessages()));
            node.put("scoreboardHeader", ConfigProcessors.component().elementFromData(mapConfig.scoreboardHeader()));
            node.put("leaderboardPosition",
                    VectorConfigProcessors.vec3I().elementFromData(mapConfig.leaderboardPosition()));
            node.putNumber("leaderboardLength", mapConfig.leaderboardLength());
            node.putNumber("worldTime", mapConfig.worldTime());
            node.putNumber("maxPlayers", mapConfig.maxPlayers());
            node.putNumber("minPlayers", mapConfig.minPlayers());
            node.putNumber("startingCoins", mapConfig.startingCoins());
            node.putNumber("repairCoins", mapConfig.repairCoins());
            node.putNumber("windowRepairRadius", mapConfig.windowRepairRadius());
            node.putNumber("powerupPickupRadius", mapConfig.powerupPickupRadius());
            node.putNumber("windowRepairTicks", mapConfig.windowRepairTicks());
            node.putNumber("corpseDeathTicks", mapConfig.corpseDeathTicks());
            node.putNumber("reviveRadius", mapConfig.reviveRadius());
            node.putBoolean("canWallshoot", mapConfig.canWallshoot());
            node.putBoolean("perksLostOnDeath", mapConfig.perksLostOnDeath());
            node.putNumber("baseReviveTicks", mapConfig.baseReviveTicks());
            node.putNumber("rollsPerChest", mapConfig.rollsPerChest());
            node.put("milestoneRounds", integerList.elementFromData(mapConfig.milestoneRounds()));
            node.put("defaultEquipment", keyList.elementFromData(mapConfig.defaultEquipment()));
            return node;
        }
    };
    private static final ConfigProcessor<List<String>> stringList = ConfigProcessor.STRING.listProcessor();
    private static final ConfigProcessor<WindowInfo> windowInfo = new ConfigProcessor<>() {
        @Override
        public @NotNull WindowInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Bounds3I frameRegion =
                    VectorConfigProcessors.bounds3I().dataFromElement(element.getElementOrThrow("frameRegion"));
            List<String> repairBlocks = stringList.dataFromElement(element.getElementOrThrow("repairBlocks"));
            Sound repairSound = ConfigProcessors.sound().dataFromElement(element.getElementOrThrow("repairSound"));
            Sound repairAllSound =
                    ConfigProcessors.sound().dataFromElement(element.getElementOrThrow("repairAllSound"));
            Sound breakSound = ConfigProcessors.sound().dataFromElement(element.getElementOrThrow("breakSound"));
            Sound breakAllSound = ConfigProcessors.sound().dataFromElement(element.getElementOrThrow("breakAllSound"));
            ConfigList repairActions = element.getListOrThrow("repairActions");
            ConfigList breakActions = element.getListOrThrow("breakActions");
            return new WindowInfo(frameRegion, repairBlocks, repairSound, repairAllSound, breakSound, breakAllSound,
                    repairActions, breakActions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull WindowInfo windowData) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(8);
            node.put("frameRegion", VectorConfigProcessors.bounds3I().elementFromData(windowData.frameRegion()));
            node.put("repairBlocks", stringList.elementFromData(windowData.repairBlocks()));
            node.put("repairSound", ConfigProcessors.sound().elementFromData(windowData.repairSound()));
            node.put("repairAllSound", ConfigProcessors.sound().elementFromData(windowData.repairAllSound()));
            node.put("breakSound", ConfigProcessors.sound().elementFromData(windowData.breakSound()));
            node.put("breakAllSound", ConfigProcessors.sound().elementFromData(windowData.breakAllSound()));
            node.put("repairActions", windowData.repairActions());
            node.put("breakActions", windowData.breakActions());
            return node;
        }
    };
    private static final ConfigProcessor<Evaluation> evaluation = ConfigProcessor.enumProcessor(Evaluation.class);
    private static final ConfigProcessor<ShopInfo> shopInfo = new ConfigProcessor<>() {
        @Override
        public ShopInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Vec3I triggerLocation =
                    VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("triggerLocation"));
            Evaluation predicateEvaluation =
                    evaluation.dataFromElement(element.getElementOrThrow("predicateEvaluation"));
            ConfigNode data = element.getNodeOrThrow("data");
            return new ShopInfo(id, triggerLocation, predicateEvaluation, data);
        }

        @Override
        public @NotNull ConfigElement elementFromData(ShopInfo shopInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put("id", ConfigProcessors.key().elementFromData(shopInfo.id()));
            node.put("triggerLocation", VectorConfigProcessors.vec3I().elementFromData(shopInfo.triggerLocation()));
            node.put("predicateEvaluation", evaluation.elementFromData(shopInfo.predicateEvaluation()));
            node.put("data", shopInfo.data());
            return node;
        }
    };

    private static final ConfigProcessor<ConfigNode> scoreboard = new ConfigProcessor<>() {
        @Override
        public @NotNull ConfigNode dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if (!element.isNode()) {
                throw new ConfigProcessException("element must be a node");
            }

            return element.asNode();
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull ConfigNode configNode) {
            return configNode;
        }
    };

    private MapProcessors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link MapSettingsInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link MapSettingsInfo} objects
     */
    public static @NotNull ConfigProcessor<MapSettingsInfo> mapInfo() {
        return mapInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link RoomInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link RoomInfo} objects
     */
    public static @NotNull ConfigProcessor<RoomInfo> roomInfo() {
        return roomInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link DoorInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link DoorInfo} objects
     */
    public static @NotNull ConfigProcessor<DoorInfo> doorInfo() {
        return doorInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link ShopInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link ShopInfo} objects
     */
    public static @NotNull ConfigProcessor<ShopInfo> shopInfo() {
        return shopInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link WindowInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link WindowInfo} objects
     */
    public static @NotNull ConfigProcessor<WindowInfo> windowInfo() {
        return windowInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link SpawnpointInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link SpawnpointInfo} objects
     */
    public static @NotNull ConfigProcessor<SpawnpointInfo> spawnpointInfo() {
        return spawnpointInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link SpawnruleInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link SpawnruleInfo} objects
     */
    public static @NotNull ConfigProcessor<SpawnruleInfo> spawnruleInfo() {
        return spawnruleInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link RoundInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link RoundInfo} objects
     */
    public static @NotNull ConfigProcessor<RoundInfo> roundInfo() {
        return roundInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link WaveInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link WaveInfo} objects
     */
    public static @NotNull ConfigProcessor<WaveInfo> waveInfo() {
        return waveInfo;
    }

    /**
     * Returns the {@link ConfigProcessor} used for serializing/deserializing {@link SpawnInfo} objects.
     *
     * @return the {@link ConfigProcessor} used for serializing/deserializing {@link SpawnInfo} objects
     */
    public static @NotNull ConfigProcessor<SpawnInfo> spawnInfo() {
        return spawnInfo;
    }

    public static @NotNull ConfigProcessor<HologramInfo> hologramInfo() {
        return hologramInfo;
    }

    public static @NotNull ConfigProcessor<Evaluation> evaluation() {
        return evaluation;
    }

    public static @NotNull ConfigProcessor<ConfigNode> scoreboard() {
        return scoreboard;
    }
}
