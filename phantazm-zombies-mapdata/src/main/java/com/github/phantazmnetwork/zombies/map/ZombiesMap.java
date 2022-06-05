package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ZombiesMap(@NotNull MapInfo info,
                         @NotNull List<RoomInfo> rooms,
                         @NotNull List<DoorInfo> doors,
                         @NotNull List<ShopInfo> shops,
                         @NotNull List<WindowInfo> windows,
                         @NotNull List<RoundInfo> rounds) { }