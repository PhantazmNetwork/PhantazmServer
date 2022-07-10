package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SpawnDistributor {
    void distributeSpawns(@NotNull Collection<SpawnInfo> spawns);
}
