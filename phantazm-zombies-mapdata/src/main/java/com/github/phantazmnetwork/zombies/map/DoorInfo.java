package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DoorInfo(@NotNull Key id,
                       @NotNull List<Key> opensTo,
                       @NotNull List<Integer> costs,
                       @NotNull List<HologramInfo> holograms,
                       @NotNull List<Region3I> regions) { }
