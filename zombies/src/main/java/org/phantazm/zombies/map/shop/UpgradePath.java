package org.phantazm.zombies.map.shop;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface UpgradePath {
    @NotNull Optional<Key> nextUpgrade(@NotNull Key key);
}
