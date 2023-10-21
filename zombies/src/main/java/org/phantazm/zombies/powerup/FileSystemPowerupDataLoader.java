package org.phantazm.zombies.powerup;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.FilesystemLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class FileSystemPowerupDataLoader extends FilesystemLoader<PowerupData> {
    private final ConfigCodec codec;
    private final ConfigProcessor<PowerupData> powerupDataConfigProcessor;

    public FileSystemPowerupDataLoader(@NotNull Path root, @NotNull ConfigCodec codec,
        @NotNull ConfigProcessor<PowerupData> powerupDataConfigProcessor) {
        super(root);
        this.codec = Objects.requireNonNull(codec);
        this.powerupDataConfigProcessor =
            Objects.requireNonNull(powerupDataConfigProcessor);
    }

    @Override
    public @NotNull PowerupData load(@NotNull String dataName) throws IOException {
        return Configuration.read(root.resolve(dataName), codec, powerupDataConfigProcessor);
    }

    @Override
    public void save(@NotNull PowerupData data) {
        throw new UnsupportedOperationException("This loader is read-only");
    }

    @Override
    public void delete(@NotNull String dataName) {
        throw new UnsupportedOperationException("This loader is read-only");
    }
}
