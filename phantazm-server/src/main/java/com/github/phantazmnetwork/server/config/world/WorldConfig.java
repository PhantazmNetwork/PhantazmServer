package com.github.phantazmnetwork.server.config.world;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for a single world.
 * @param spawnPoint The spawn point for the world
 */
public record WorldConfig(@NotNull Pos spawnPoint) {

    /**
     * Creates config regarding a single world.
     * @param spawnPoint The spawn point for the world
     */
    public WorldConfig {
        Objects.requireNonNull(spawnPoint, "spawnPoint");
    }

}
