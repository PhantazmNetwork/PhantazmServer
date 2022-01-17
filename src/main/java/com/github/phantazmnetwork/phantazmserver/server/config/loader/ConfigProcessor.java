package com.github.phantazmnetwork.phantazmserver.server.config.loader;

import com.github.phantazmnetwork.phantazmserver.server.config.server.PingListConfig;
import com.github.phantazmnetwork.phantazmserver.server.config.server.ServerConfig;
import com.github.phantazmnetwork.phantazmserver.server.config.server.ServerInfoConfig;
import com.github.phantazmnetwork.phantazmserver.server.config.world.WorldConfig;
import com.github.phantazmnetwork.phantazmserver.server.config.world.WorldsConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes configuration from a {@link Path}
 * @param <T> The type of configuration to process
 */
public interface ConfigProcessor<T> {

    /**
     * Creates a processor for server configuration
     * @param miniMessage A {@link MiniMessage} instance used to parse {@link Component}s
     * @return A new server config processor
     */
    static @NotNull ConfigProcessor<ServerConfig> serverConfigProcessor(@NotNull MiniMessage miniMessage) {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull ServerConfig createConfigFromNode(@NotNull ConfigNode configNode)
                    throws ConfigReadException {
                try {
                    ConfigNode serverInfo = configNode.getNodeOrDefault(LinkedConfigNode::new, "serverInfo");
                    String serverIP = serverInfo.getStringOrDefault("0.0.0.0", "serverIP");
                    int port = serverInfo.getNumberOrDefault(25565, "port").intValue();
                    boolean mojangAuthEnabled = serverInfo
                            .getBooleanOrDefault(true, "mojangAuthEnabled");
                    boolean optifineEnabled = serverInfo.getBooleanOrDefault(true, "optifineEnabled");
                    ServerInfoConfig serverInfoConfig = new ServerInfoConfig(serverIP, port, mojangAuthEnabled,
                            optifineEnabled);

                    ConfigNode pingList = configNode.getNodeOrDefault(LinkedConfigNode::new, "pingList");
                    Component description = miniMessage.parse(pingList.getStringOrDefault("",
                            "description"));
                    PingListConfig pingListConfig = new PingListConfig(description);

                    return new ServerConfig(serverInfoConfig, pingListConfig);
                }
                catch (IllegalStateException e) {
                    throw new ConfigReadException(e);
                }
            }

            @Override
            public @NotNull ConfigNode createNodeFromConfig(@NotNull ServerConfig config) {
                ConfigNode serverInfo = new LinkedConfigNode();
                ServerInfoConfig serverInfoConfig = config.serverInfoConfig();
                serverInfo.put("serverIP", new ConfigPrimitive(serverInfoConfig.serverIP()));
                serverInfo.put("port", new ConfigPrimitive(serverInfoConfig.port()));
                serverInfo.put("mojangAuthEnabled", new ConfigPrimitive(serverInfoConfig.mojangAuthEnabled()));
                serverInfo.put("optifineEnabled", new ConfigPrimitive(serverInfoConfig.optifineEnabled()));

                ConfigNode pingList = new LinkedConfigNode();
                PingListConfig pingListConfig = config.pingListConfig();
                String description = miniMessage.serialize(pingListConfig.description());
                pingList.put("description", new ConfigPrimitive(description));

                ConfigNode configNode = new LinkedConfigNode();
                configNode.put("serverInfo", serverInfo);
                configNode.put("pingList", pingList);

                return configNode;
            }
        };
    }

    /**
     * Creates a processor for worlds configuration
     * @return A new worlds config processor
     */
    static @NotNull ConfigProcessor<WorldsConfig> worldsConfigProcessor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull WorldsConfig createConfigFromNode(@NotNull ConfigNode configNode)
                    throws ConfigReadException {
                try {
                    String defaultWorldName = configNode
                            .getStringOrDefault("lobby", "defaultWorldName");
                    String worldsPath = configNode.getStringOrDefault("./worlds/", "worldsPath");
                    String mapsPath = configNode.getStringOrDefault("./maps/", "mapsPath");

                    Map<String, WorldConfig> worlds = new HashMap<>();
                    ConfigNode worldsNode = configNode.getNodeOrDefault(LinkedConfigNode::new, "worlds");
                    for (Map.Entry<String, ConfigElement> world : worldsNode.entrySet()) {
                        ConfigNode worldNode = world.getValue().asNode();
                        ConfigNode spawnPoint = worldNode.get("spawnPoint").asNode();

                        double x = spawnPoint.get("x").asNumber().doubleValue();
                        double y = spawnPoint.get("y").asNumber().doubleValue();
                        double z = spawnPoint.get("z").asNumber().doubleValue();
                        float yaw = spawnPoint.getNumberOrDefault(0.0F, "yaw").floatValue();
                        float pitch = spawnPoint.getNumberOrDefault(0.0F, "pitch").floatValue();

                        worlds.put(world.getKey(), new WorldConfig(new Pos(x, y, z, yaw, pitch)));
                    }

                    return new WorldsConfig(defaultWorldName, worldsPath, mapsPath, worlds);
                }
                catch (IllegalStateException e) {
                    throw new ConfigReadException(e);
                }
            }

            @Override
            public @NotNull ConfigNode createNodeFromConfig(@NotNull WorldsConfig config) {
                ConfigNode configNode = new LinkedConfigNode();
                configNode.put("defaultWorldName", new ConfigPrimitive(config.defaultWorldName()));
                configNode.put("worldsPath", new ConfigPrimitive(config.worldsPath()));
                configNode.put("mapsPath", new ConfigPrimitive(config.mapsPath()));

                ConfigNode worldsNode = new LinkedConfigNode();
                for (Map.Entry<String, WorldConfig> worldConfigEntry : config.worlds().entrySet()) {
                    WorldConfig worldConfig = worldConfigEntry.getValue();

                    ConfigNode spawnPointNode = new LinkedConfigNode();
                    Pos spawnPoint = worldConfig.spawnPoint();
                    spawnPointNode.put("x", new ConfigPrimitive(spawnPoint.x()));
                    spawnPointNode.put("y", new ConfigPrimitive(spawnPoint.y()));
                    spawnPointNode.put("z", new ConfigPrimitive(spawnPoint.z()));
                    spawnPointNode.put("yaw", new ConfigPrimitive(spawnPoint.yaw()));
                    spawnPointNode.put("pitch", new ConfigPrimitive(spawnPoint.pitch()));

                    ConfigNode worldNode = new LinkedConfigNode();
                    worldNode.put("spawnPoint", spawnPointNode);

                    worldsNode.put(worldConfigEntry.getKey(), worldNode);
                }
                configNode.put("worlds", worldsNode);

                return configNode;
            }
        };
    }

    /**
     * Converts a {@link  ConfigNode} to config
     * @param configNode The {@link ConfigNode} to convert from
     * @return The config
     * @throws ConfigReadException If creating a config failed
     */
    @NotNull T createConfigFromNode(@NotNull ConfigNode configNode) throws ConfigReadException;

    /**
     * Converts config to a {@link ConfigNode}
     * @param config The config to write
     */
    @NotNull ConfigNode createNodeFromConfig(@NotNull T config);

}
