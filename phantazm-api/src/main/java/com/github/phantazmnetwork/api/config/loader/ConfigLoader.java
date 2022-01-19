package com.github.phantazmnetwork.api.config.loader;

import org.jetbrains.annotations.NotNull;

/**
 * Loader for configuration
 * @param <TConfig> The type of configuration to use
 */
public interface ConfigLoader<TConfig> {

    /**
     * Loads the config and writes a default one if necessary
     * @return The config
     * @throws ConfigReadException if parsing the configuration failed
     * @throws ConfigWriteException if writing the default configuration failed
     */
    @NotNull TConfig load() throws ConfigReadException, ConfigWriteException;

}
