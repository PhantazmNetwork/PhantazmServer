package org.phantazm.core.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface UpgradePath {
    @NotNull Optional<Key> nextUpgrade(@NotNull Key key);
}
