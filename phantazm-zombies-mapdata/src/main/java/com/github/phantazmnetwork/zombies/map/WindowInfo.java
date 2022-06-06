package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record WindowInfo(@NotNull Key room,
                         @NotNull RegionInfo frameRegion,
                         @NotNull Key repairSound,
                         @NotNull Key repairAllSound,
                         @NotNull Key breakSound,
                         @NotNull Key breakAllSound) { }
