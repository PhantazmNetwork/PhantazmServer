package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RoomInfo(@NotNull Key id, @NotNull String displayName, @NotNull List<RegionInfo> regions) { }
