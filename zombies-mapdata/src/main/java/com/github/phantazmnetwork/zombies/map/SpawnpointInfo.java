package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines a spawnpoint.
 */
public record SpawnpointInfo(@NotNull Vec3I position, @NotNull Key spawnRule) {
    /**
     * Creates a new instance of this record.
     *
     * @param position  the position of the spawnpoint
     * @param spawnRule the id of the spawnrule which will be used to dictate what may spawn here
     */
    public SpawnpointInfo {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(spawnRule, "spawnRule");
    }
}
