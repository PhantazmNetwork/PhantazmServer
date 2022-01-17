package com.github.zapv3.server.config.loader;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Loader for configuration
 * @param <T> The type of configuration to use
 */
public interface ConfigLoader<T> {

    /**
     * Creates a default config loader
     * @param path The path to load config from
     * @param processor The processor to parse config
     * @param defaultConfigSupplier Supplier for a default config
     * @param <T> The type of config to load
     * @return The config
     */
    static <T> @NotNull ConfigLoader<T> defaultLoader(@NotNull Path path, @NotNull ConfigProcessor<T> processor,
                                                      @NotNull Supplier<T> defaultConfigSupplier) {
        return () -> {
            if (!Files.exists(path)) {
                T config = defaultConfigSupplier.get();
                processor.writeConfig(path, config);

                return config;
            }

            if (!Files.isRegularFile(path)) {
                throw new ConfigReadException("Path " + path + " is not a file!");
            }

            return processor.readConfig(path);
        };
    }

    /**
     * Loads the config and writes a default one if necessary
     * @return The config
     * @throws ConfigReadException if parsing the configuration failed
     * @throws ConfigWriteException if writing the default configuration failed
     */
    @NotNull T load() throws ConfigReadException, ConfigWriteException;

}
