package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RoomInfo(@NotNull String name, @NotNull String displayName, @NotNull List<RegionInfo> regions) { }
