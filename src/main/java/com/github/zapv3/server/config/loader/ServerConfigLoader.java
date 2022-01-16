package com.github.zapv3.server.config.loader;

import com.github.zapv3.server.config.ServerConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface ServerConfigLoader {

    static @NotNull ServerConfigLoader defaultLoader(@NotNull Path path, @NotNull ServerConfigProcessor processor,
                                                     @NotNull Supplier<ServerConfig> configSupplier) {
        return () -> {
            if (!Files.exists(path)) {
                ServerConfig config = configSupplier.get();
                processor.writeConfig(path, config);

                return config;
            }

            if (!Files.isRegularFile(path)) {
                throw new ServerConfigReadException("Path " + path + " is not a file!");
            }

            return processor.readConfig(path);
        };
    }

    @NotNull ServerConfig load() throws ServerConfigReadException, ServerConfigWriteException;

}
