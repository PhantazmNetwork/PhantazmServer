package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public record RegionInfo(@NotNull Vec3I origin,
                         @NotNull Vec3I lengths) { }
