package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WaveInfo(@NotNull List<SpawnInfo> spawns) {
}
