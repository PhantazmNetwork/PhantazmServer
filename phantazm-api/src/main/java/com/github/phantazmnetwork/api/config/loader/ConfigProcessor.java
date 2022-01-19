package com.github.phantazmnetwork.api.config.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Processes configuration from a {@link Path}.
 * @param <T> The type of configuration to process
 */
public interface ConfigProcessor<T> {

    /**
     * Converts a {@link ConfigElement} to config.
     * @param configElement The {@link ConfigElement} to convert from
     * @return The config
     * @throws ConfigReadException If creating a config failed
     */
    @NotNull T createConfigFromElement(@NotNull ConfigElement configElement) throws ConfigReadException;

    /**
     * Converts config to a {@link ConfigElement}.
     * @param config The config to write
     */
    @NotNull ConfigElement createNodeFromConfig(@NotNull T config);

}
