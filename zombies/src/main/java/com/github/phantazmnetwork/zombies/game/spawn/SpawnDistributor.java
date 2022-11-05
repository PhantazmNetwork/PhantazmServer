package com.github.phantazmnetwork.zombies.game.spawn;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.map.Spawnpoint;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface SpawnDistributor {
    @NotNull List<PhantazmMob> distributeSpawns(@NotNull List<? extends Spawnpoint> spawnpoints,
            @NotNull Collection<? extends SpawnInfo> spawns);
}
