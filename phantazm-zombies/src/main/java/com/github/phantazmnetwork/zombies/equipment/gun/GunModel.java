package com.github.phantazmnetwork.zombies.equipment.gun;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * A model that represents a gun.
 * @param rootLevel The root level of the gun's upgrade tree
 * @param levels A {@link Map} of level {@link Key}s to the gun's possible {@link GunLevel}s
 */
public record GunModel(@NotNull Key rootLevel, @NotNull Map<Key, GunLevel> levels) {

    /**
     * Creates a {@link GunModel}.
     * @param rootLevel The root level of the gun's upgrade tree
     * @param levels A {@link Map} of level {@link Key}s to the gun's possible {@link GunLevel}s
     */
    public GunModel {
        Objects.requireNonNull(rootLevel, "rootLevel");
        Objects.requireNonNull(levels, "levels");

        for (GunLevel level : levels.values()) {
            Objects.requireNonNull(level, "levels level");
        }
    }

}
