package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.world.WorldConfig;
import com.github.phantazmnetwork.server.config.world.WorldsConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Config processor used for {@link WorldsConfig}s.
 */
public class WorldsConfigProcessor implements ConfigProcessor<WorldsConfig> {
    @Override
    public @NotNull WorldsConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        try {
            String defaultWorldName = element.getStringOrDefault(WorldsConfig.DEFAULT_DEFAULT_WORLD_NAME,
                    "defaultWorldName");

            Path worldsPath = Paths.get(element.getStringOrDefault(WorldsConfig.DEFAULT_WORLDS_PATH_STRING,
                    "worldsPath"));
            Path mapsPath = Paths.get(element.getStringOrDefault(WorldsConfig.DEFAULT_MAPS_PATH_STRING,
                    "mapsPath"));

            Map<String, WorldConfig> worlds = new HashMap<>();
            ConfigNode worldsNode = element.getNodeOrDefault(LinkedConfigNode::new, "worlds");
            for (Map.Entry<String, ConfigElement> world : worldsNode.entrySet()) {
                ConfigNode worldNode = world.getValue().asNode();
                ConfigNode spawnPoint = worldNode.get("spawnPoint").asNode();

                double x = spawnPoint.getNumberOrDefault(WorldConfig.DEFAULT_POS.x(), "x").doubleValue();
                double y = spawnPoint.getNumberOrDefault(WorldConfig.DEFAULT_POS.y(), "y").doubleValue();
                double z = spawnPoint.getNumberOrDefault(WorldConfig.DEFAULT_POS.z(), "z").doubleValue();

                float yaw = spawnPoint.getNumberOrDefault(WorldConfig.DEFAULT_POS.yaw(), "yaw").floatValue();
                float pitch = spawnPoint.getNumberOrDefault(WorldConfig.DEFAULT_POS.pitch(), "pitch")
                        .floatValue();

                worlds.put(world.getKey(), new WorldConfig(new Pos(x, y, z, yaw, pitch)));
            }

            return new WorldsConfig(defaultWorldName, worldsPath, mapsPath, Map.copyOf(worlds));
        }
        catch (IllegalStateException | InvalidPathException e) {
            throw new ConfigProcessException(e);
        }
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull WorldsConfig worldsConfig) {
        ConfigNode configNode = new LinkedConfigNode();
        configNode.put("defaultWorldName", new ConfigPrimitive(worldsConfig.defaultWorldName()));
        configNode.put("worldsPath", new ConfigPrimitive(worldsConfig.worldsPath().toString()));
        configNode.put("mapsPath", new ConfigPrimitive(worldsConfig.mapsPath().toString()));

        ConfigNode worldsNode = new LinkedConfigNode();
        for (Map.Entry<String, WorldConfig> worldConfigEntry : worldsConfig.worlds().entrySet()) {
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
