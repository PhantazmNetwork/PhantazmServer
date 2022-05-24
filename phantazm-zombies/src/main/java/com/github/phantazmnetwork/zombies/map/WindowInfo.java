package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WindowInfo(@NotNull String roomName, @NotNull RegionInfo frameRegion,
                         @NotNull List<RegionInfo> internalRegions, @NotNull Vec3I spawn, @NotNull Vec3I target) { }
