package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RoomInfo(@NotNull Key id,
                       @NotNull Component displayName,
                       @NotNull List<RegionInfo> regions) { }
