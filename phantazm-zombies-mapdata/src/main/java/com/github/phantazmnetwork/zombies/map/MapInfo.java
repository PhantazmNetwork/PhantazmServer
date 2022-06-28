package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MapInfo(@NotNull MapSettingsInfo info,
                      @NotNull List<RoomInfo> rooms,
                      @NotNull List<DoorInfo> doors,
                      @NotNull List<ShopInfo> shops,
                      @NotNull List<WindowInfo> windows,
                      @NotNull List<RoundInfo> rounds,
                      @NotNull List<SpawnruleInfo> spawnrules,
                      @NotNull List<SpawnpointInfo> spawnpoints) { }