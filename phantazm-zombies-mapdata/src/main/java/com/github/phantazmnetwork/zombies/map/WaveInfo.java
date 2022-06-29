package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WaveInfo(int delayTicks, @NotNull List<SpawnInfo> spawns) { }
