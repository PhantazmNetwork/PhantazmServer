package com.github.phantazmnetwork.phantazmserver.server.config.loader;

import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads config using the file system
 * @param <ConfigType> The type of configuration to load
 */
@SuppressWarnings("ClassCanBeRecord")
public class FileSystemConfigLoader<ConfigType> implements ConfigLoader<ConfigType> {

    private final Path path;

    private final ConfigCodec codec;

    private final ConfigProcessor<ConfigType> processor;

    public FileSystemConfigLoader(
            @NotNull Path path,
            @NotNull ConfigCodec codec,
            @NotNull ConfigProcessor<ConfigType> processor
    ) {
        this.path = path;
        this.codec = codec;
        this.processor = processor;
    }

    @NotNull
    @Override
    public ConfigType load() throws ConfigReadException, ConfigWriteException {
        if (!Files.exists(path)) {
            ConfigType config = processor.createConfigFromNode(new LinkedConfigNode());
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
    }

}
