package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SpawnruleInfo(@NotNull Key id, @NotNull List<Key> spawns, boolean isBlacklist) { }
