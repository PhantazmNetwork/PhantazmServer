package com.github.phantazmnetwork.zombies.map.shop;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@FunctionalInterface
public interface UpgradePath {
    @NotNull Optional<Key> nextUpgrade(@NotNull Key key);
}
