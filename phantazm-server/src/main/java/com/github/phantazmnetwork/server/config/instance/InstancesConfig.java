package com.github.phantazmnetwork.server.config.instance;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Config for the server's {@link net.minestom.server.instance.Instance}s.
 * @param defaultInstanceName The default {@link net.minestom.server.instance.Instance}'s name (the lobby {@link net.minestom.server.instance.Instance}'s name)
 * @param instancesPath The path to all {@link net.minestom.server.instance.Instance}s
 * @param mapsPath The path to all maps
 * @param instances A map of {@link net.minestom.server.instance.Instance} configuration
 */
public record InstancesConfig(@NotNull String defaultInstanceName,
                              @NotNull Path instancesPath,
                              @NotNull Path mapsPath,
                              @NotNull Map<String, InstanceConfig> instances) {
    /**
     * The default {@link net.minestom.server.instance.Instance} name.
     */
    public static final String DEFAULT_INSTANCE_NAME = "instance";

    /**
     * The default location used to store {@link net.minestom.server.instance.Instance}.
     */
    public static final String DEFAULT_INSTANCES_PATH_STRING = "./instances/";

    /**
     * The default location used to store maps.
     */
    public static final String DEFAULT_MAPS_PATH_STRING = "./maps/";

    /**
     * The default InstancesConfig instance.
     */
    public static final InstancesConfig DEFAULT = new InstancesConfig(DEFAULT_INSTANCE_NAME,
            Path.of(DEFAULT_INSTANCES_PATH_STRING), Path.of(DEFAULT_MAPS_PATH_STRING), Collections.emptyMap());

    /**
     * Creates config for the server's {@link net.minestom.server.instance.Instance}s.
     * @param defaultInstanceName The default {@link InstancesConfig}'s name (the lobby {@link net.minestom.server.instance.Instance}'s name)
     * @param instancesPath The path to all {@link net.minestom.server.instance.Instance}s
     * @param mapsPath The path to all maps
     * @param instances A map of {@link net.minestom.server.instance.Instance} configuration
     */
    public InstancesConfig {
        Objects.requireNonNull(defaultInstanceName, "defaultInstanceName");
        Objects.requireNonNull(instancesPath, "instancesPath");
        Objects.requireNonNull(mapsPath, "mapsPath");
        Objects.requireNonNull(instances, "instances");
    }

}
