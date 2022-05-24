package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MapData(@NotNull MapInfo info, @NotNull List<RoomInfo> rooms, @NotNull List<DoorInfo> doors,
                      @NotNull List<WindowInfo> windows, @NotNull List<RoundInfo> rounds) { }