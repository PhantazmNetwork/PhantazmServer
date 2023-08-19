package org.phantazm.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines a particular type and amount of mobs to spawn.
 */
public record SpawnInfo(@NotNull Key id, @NotNull Key spawnType, int amount) {
    /**
     * Creates a new instance of this record.
     *
     * @param id     the id of the mob to spawn
     * @param amount the amount of mobs to spawn
     */
    public SpawnInfo {
        Objects.requireNonNull(id);
    }
}
