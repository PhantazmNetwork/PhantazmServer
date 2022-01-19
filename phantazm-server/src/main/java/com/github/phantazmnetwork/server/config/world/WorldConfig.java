package com.github.phantazmnetwork.server.config.world;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

/**
 * Config for a single world
 * @param spawnPoint The spawn point for the world
 */
public record WorldConfig(@NotNull Pos spawnPoint) {

}
