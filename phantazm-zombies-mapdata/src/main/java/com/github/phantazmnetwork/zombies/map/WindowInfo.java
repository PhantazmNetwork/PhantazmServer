package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WindowInfo(@NotNull Region3I frameRegion,
                         @NotNull List<String> repairBlocks,
                         @NotNull Key repairSound,
                         @NotNull Key repairAllSound,
                         @NotNull Key breakSound,
                         @NotNull Key breakAllSound) { }
