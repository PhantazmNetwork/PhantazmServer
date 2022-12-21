package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.map.FilesystemLoader;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class FileSystemPowerupLoader extends FilesystemLoader<PowerupInfo> {
    private static final ConfigProcessor<PowerupInfo> powerupInfo = new ConfigProcessor<>() {
        @Override
        public PowerupInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = ConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            ConfigList visuals = element.getListOrThrow("visuals");
            ConfigList actions = element.getListOrThrow("actions");
            ConfigNode deactivationPredicate = element.getNodeOrThrow("deactivationPredicate");
            return new PowerupInfo(id, visuals, actions, deactivationPredicate);
        }

        @Override
        public @NotNull ConfigElement elementFromData(PowerupInfo powerupInfo) throws ConfigProcessException {
            return ConfigNode.of("id", ConfigProcessors.key().elementFromData(powerupInfo.id()), "visuals",
                    powerupInfo.visuals(), "actions", powerupInfo.actions(), "deactivationPredicate",
                    powerupInfo.deactivationPredicate());
        }
    };

    private final ConfigCodec codec;

    public FileSystemPowerupLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        super(root);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public @NotNull PowerupInfo load(@NotNull String dataName) throws IOException {
        return Configuration.read(root.resolve(dataName), codec, powerupInfo);
    }

    @Override
    public void save(@NotNull PowerupInfo data) {
        throw new UnsupportedOperationException("This loader is read-only");
    }

    @Override
    public void delete(@NotNull String dataName) {
        throw new UnsupportedOperationException("This loader is read-only");
    }
}
