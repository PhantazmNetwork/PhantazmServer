package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.server.config.instance.InstancesConfig;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Config processor used for {@link InstancesConfig}s.
 */
public class WorldsConfigProcessor implements ConfigProcessor<InstancesConfig> {
    @Override
    public @NotNull InstancesConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        try {
            String defaultInstanceName = element.getStringOrDefault(InstancesConfig.DEFAULT_INSTANCE_NAME,
                    "defaultInstanceName");

            Path instancesPath = Path.of(element.getStringOrDefault(InstancesConfig.DEFAULT_INSTANCES_PATH_STRING,
                    "instancesPath"));
            Path mapsPath = Path.of(element.getStringOrDefault(InstancesConfig.DEFAULT_MAPS_PATH_STRING,
                    "mapsPath"));

            Map<String, InstanceConfig> instances = new HashMap<>();
            ConfigNode instancesNode = element.getNodeOrDefault(LinkedConfigNode::new, "instances");
            for (Map.Entry<String, ConfigElement> instance : instancesNode.entrySet()) {
                ConfigNode instanceNode = instance.getValue().asNode();
                ConfigNode spawnPoint = instanceNode.get("spawnPoint").asNode();

                double x = spawnPoint.getNumberOrDefault(InstanceConfig.DEFAULT_POS.x(), "x").doubleValue();
                double y = spawnPoint.getNumberOrDefault(InstanceConfig.DEFAULT_POS.y(), "y").doubleValue();
                double z = spawnPoint.getNumberOrDefault(InstanceConfig.DEFAULT_POS.z(), "z").doubleValue();
                float yaw = spawnPoint.getNumberOrDefault(InstanceConfig.DEFAULT_POS.yaw(), "yaw").floatValue();
                float pitch = spawnPoint.getNumberOrDefault(InstanceConfig.DEFAULT_POS.pitch(), "pitch")
                        .floatValue();

                instances.put(instance.getKey(), new InstanceConfig(new Pos(x, y, z, yaw, pitch)));
            }

            return new InstancesConfig(defaultInstanceName, instancesPath, mapsPath, Map.copyOf(instances));
        }
        catch (IllegalStateException | InvalidPathException e) {
            throw new ConfigProcessException(e);
        }
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull InstancesConfig instancesConfig) {
        ConfigNode configNode = new LinkedConfigNode();
        configNode.put("defaultInstanceName", new ConfigPrimitive(instancesConfig.defaultInstanceName()));
        configNode.put("instancesPath", new ConfigPrimitive(instancesConfig.instancesPath().toString()));
        configNode.put("mapsPath", new ConfigPrimitive(instancesConfig.mapsPath().toString()));

        ConfigNode instancesNode = new LinkedConfigNode();
        for (Map.Entry<String, InstanceConfig> instanceConfigEntry : instancesConfig.instances().entrySet()) {
            InstanceConfig instanceConfig = instanceConfigEntry.getValue();

            ConfigNode spawnPointNode = new LinkedConfigNode();
            Pos spawnPoint = instanceConfig.spawnPoint();
            spawnPointNode.put("x", new ConfigPrimitive(spawnPoint.x()));
            spawnPointNode.put("y", new ConfigPrimitive(spawnPoint.y()));
            spawnPointNode.put("z", new ConfigPrimitive(spawnPoint.z()));
            spawnPointNode.put("yaw", new ConfigPrimitive(spawnPoint.yaw()));
            spawnPointNode.put("pitch", new ConfigPrimitive(spawnPoint.pitch()));

            ConfigNode instanceNode = new LinkedConfigNode();
            instanceNode.put("spawnPoint", spawnPointNode);

            instancesNode.put(instanceConfigEntry.getKey(), instanceNode);
        }
        configNode.put("instances", instancesNode);

        return configNode;
    }
}
