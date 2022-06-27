package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record SpawnInfo(@NotNull Key id,
                        int amount) { }
