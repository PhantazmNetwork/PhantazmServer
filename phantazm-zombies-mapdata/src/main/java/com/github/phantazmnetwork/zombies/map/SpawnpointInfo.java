package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record SpawnpointInfo(@NotNull Vec3I position, @NotNull Key spawnRule, @NotNull SpawnType type) { }