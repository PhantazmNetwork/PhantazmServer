package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DoorInfo(int cost,
                       @NotNull List<Key> opensTo,
                       @NotNull List<RegionInfo> doorRegions) { }
