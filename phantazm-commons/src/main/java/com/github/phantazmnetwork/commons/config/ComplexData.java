package com.github.phantazmnetwork.commons.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public record ComplexData(@NotNull Key mainKey, @NotNull Map<Key, Keyed> objects) {

    public ComplexData {
        Objects.requireNonNull(mainKey, "mainKey");
        Objects.requireNonNull(objects, "objects");
    }

}
