package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WindowInfo(@NotNull Key room,
                         @NotNull RegionInfo frameRegion,
                         @NotNull List<RegionInfo> internalRegions,
                         @NotNull Key repairSound,
                         @NotNull Key repairAllSound,
                         @NotNull Key breakSound,
                         @NotNull Key breakAllSound) { }
