package com.github.phantazmnetwork.api.config.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Loads config using the file system.
 * @param <TConfig> The type of configuration to load
 */
@SuppressWarnings("ClassCanBeRecord")
public class FileSystemConfigLoader<TConfig> implements ConfigLoader<TConfig> {

    private final Path path;

    private final ConfigCodec codec;

    private final ConfigProcessor<TConfig> processor;

    /**
     * Creates a config loader using the file system.
     * @param path The {@link Path} of the config
     * @param codec The {@link ConfigCodec} to use for reading and writing
     * @param processor A {@link ConfigProcessor} to read and write config
     */
    public FileSystemConfigLoader(@NotNull Path path,
                                  @NotNull ConfigCodec codec,
                                  @NotNull ConfigProcessor<TConfig> processor) {
        this.path = Objects.requireNonNull(path, "path");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.processor = Objects.requireNonNull(processor, "processor");
    }

    @Override
    public @NotNull TConfig load() throws ConfigReadException, ConfigWriteException {
        if (!Files.exists(path)) {
            TConfig config = processor.createConfigFromElement(new LinkedConfigNode());
            ConfigElement configElement = processor.createNodeFromConfig(config);

            try {
                ConfigBridges.write(Files.newOutputStream(path), codec, configElement);
            }
            catch (IOException e) {
                throw new ConfigWriteException(e);
            }

            return config;
        }

        if (!Files.isRegularFile(path)) {
            throw new ConfigReadException("Path " + path + " is not a file");
        }

        try {
            ConfigElement configNode = ConfigBridges.read(Files.newInputStream(path), codec);

            return processor.createConfigFromElement(configNode);
        }
        catch (IOException e) {
            throw new ConfigReadException(e);
        }
    }

}
