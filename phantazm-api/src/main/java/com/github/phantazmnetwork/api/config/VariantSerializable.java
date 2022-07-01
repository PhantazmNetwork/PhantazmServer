package com.github.phantazmnetwork.api.config;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface VariantSerializable {
    @NotNull Key getSerialKey();
}
