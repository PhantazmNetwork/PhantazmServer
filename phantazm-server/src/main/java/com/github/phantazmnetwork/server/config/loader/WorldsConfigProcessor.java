package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.api.config.loader.ConfigProcessor;
import com.github.phantazmnetwork.api.config.loader.ConfigReadException;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.world.WorldConfig;
import com.github.phantazmnetwork.server.config.world.WorldsConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Config processor used for {@link ServerConfig}s
 */
public class WorldsConfigProcessor implements ConfigProcessor<WorldsConfig> {

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

}
