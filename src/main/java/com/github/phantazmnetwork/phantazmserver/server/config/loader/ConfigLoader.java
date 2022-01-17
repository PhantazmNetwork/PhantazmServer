package com.github.phantazmnetwork.phantazmserver.server.config.loader;

import org.jetbrains.annotations.NotNull;

/**
 * Loader for configuration
 * @param <ConfigType> The type of configuration to use
 */
public interface ConfigLoader<ConfigType> {

    /**
     * Loads the config and writes a default one if necessary
     * @return The config
     * @throws ConfigReadException if parsing the configuration failed
     * @throws ConfigWriteException if writing the default configuration failed
     */
    @NotNull ConfigType load() throws ConfigReadException, ConfigWriteException;

}
