package com.github.zapv3.server.config.loader;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface ConfigLoader<T> {

    static <T> @NotNull ConfigLoader<T> defaultLoader(@NotNull Path path, @NotNull ConfigProcessor<T> processor,
                                               @NotNull Supplier<T> configSupplier) {
        return () -> {
            if (!Files.exists(path)) {
                T config = configSupplier.get();
                processor.writeConfig(path, config);

                return config;
            }

            if (!Files.isRegularFile(path)) {
                throw new ConfigReadException("Path " + path + " is not a file!");
            }

            return processor.readConfig(path);
        };
    }

    @NotNull T load() throws ConfigReadException, ConfigWriteException;

}
