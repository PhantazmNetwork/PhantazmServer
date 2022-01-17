package com.github.zapv3.server.config.loader;

import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
     * @param codec The codec to write or read {@link ConfigNode}s with
     * @param processor The processor to parse config
     * @param defaultConfigSupplier Supplier for a default config
     * @param <T> The type of config to load
     * @return The config
     */
    static <T> @NotNull ConfigLoader<T> defaultLoader(@NotNull Path path, @NotNull ConfigCodec codec,
                                                      @NotNull ConfigProcessor<T> processor,
                                                      @NotNull Supplier<T> defaultConfigSupplier) {
        return () -> {
            if (!Files.exists(path)) {
                T config = defaultConfigSupplier.get();
                ConfigNode configNode = processor.createNodeFromConfig(config);

                try {
                    ConfigBridges.write(Files.newOutputStream(path), codec, configNode);
                }
                catch (IOException e) {
                    throw new ConfigWriteException(e);
                }

                return config;
            }

            if (!Files.isRegularFile(path)) {
                throw new ConfigReadException("Path " + path + " is not a file!");
            }

            try {
                ConfigNode configNode = ConfigBridges.read(Files.newInputStream(path), codec).asNode();

                return processor.createConfigFromNode(configNode);
            }
            catch (IOException e) {
                throw new ConfigReadException(e);
            }
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
