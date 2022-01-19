package com.github.phantazmnetwork.api.config.loader;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Processes configuration from a {@link Path}
 * @param <T> The type of configuration to process
 */
public interface ConfigProcessor<T> {

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
