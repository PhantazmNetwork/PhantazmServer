package org.phantazm.zombies.spawn;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.map.SpawnInfo;
import org.phantazm.zombies.map.Spawnpoint;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface SpawnDistributor {
    @NotNull List<PhantazmMob> distributeSpawns(@NotNull List<? extends Spawnpoint> spawnpoints,
            @NotNull Collection<? extends SpawnInfo> spawns);
}
