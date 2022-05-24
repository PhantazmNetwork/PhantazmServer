package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DoorInfo(int cost, @NotNull List<String> opensTo, @NotNull List<RegionInfo> doorRegions) { }
