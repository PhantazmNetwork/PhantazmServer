package org.phantazm.zombies.map;

import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Defines a spawnpoint.
 */
public record SpawnpointInfo(@NotNull Vec3I position,
                             @NotNull Key spawnRule,
                             boolean linkToWindow,
                             @Nullable Vec3I linkedWindow) {
    /**
     * Creates a new instance of this record.
     *
     * @param position     the position of the spawnpoint
     * @param spawnRule    the id of the spawnrule which will be used to dictate what may spawn here
     * @param linkToWindow whether to try and link this spawnpoint up with the closest window
     */
    public SpawnpointInfo {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(spawnRule, "spawnRule");
    }
}
