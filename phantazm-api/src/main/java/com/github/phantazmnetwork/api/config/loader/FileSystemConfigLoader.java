package com.github.phantazmnetwork.api.config.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads config using the file system.
 * @param <TConfig> The type of configuration to load
 */
@SuppressWarnings("ClassCanBeRecord")
public class FileSystemConfigLoader<TConfig> implements ConfigLoader<TConfig> {

    private final Path path;

    private final ConfigCodec codec;

    private final ConfigProcessor<TConfig> processor;

    public FileSystemConfigLoader(
            @NotNull Path path,
            @NotNull ConfigCodec codec,
            @NotNull ConfigProcessor<TConfig> processor
    ) {
        this.path = path;
        this.codec = codec;
        this.processor = processor;
    }

    @NotNull
    @Override
    public TConfig load() throws ConfigReadException, ConfigWriteException {
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
