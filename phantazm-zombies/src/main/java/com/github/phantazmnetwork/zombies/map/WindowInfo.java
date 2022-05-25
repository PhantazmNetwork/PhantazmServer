package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WindowInfo(@NotNull Key room, @NotNull RegionInfo frameRegion,
                         @NotNull List<RegionInfo> internalRegions, @NotNull Vec3I spawn, @NotNull Vec3I target) { }
