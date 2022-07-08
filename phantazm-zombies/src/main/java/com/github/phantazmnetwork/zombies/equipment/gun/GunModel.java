package com.github.phantazmnetwork.zombies.equipment.gun;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public record GunModel(@NotNull Key rootLevel,
                       @NotNull Map<Key, GunLevel> levels) {

    public GunModel {
        Objects.requireNonNull(rootLevel, "rootLevel");
        Objects.requireNonNull(levels, "levels");

        for (GunLevel level : levels.values()) {
            Objects.requireNonNull(level, "levels level");
        }
    }

}
