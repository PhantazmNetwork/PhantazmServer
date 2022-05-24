package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RoundInfo(int round, @NotNull List<WaveInfo> waves) { }
