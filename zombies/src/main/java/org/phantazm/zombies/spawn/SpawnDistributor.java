package org.phantazm.zombies.spawn;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.SpawnInfo;
import org.phantazm.zombies.map.Spawnpoint;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface SpawnDistributor {
    @NotNull List<Mob> distributeSpawns(@NotNull List<? extends Spawnpoint> spawnpoints,
        @NotNull Collection<? extends SpawnInfo> spawns);
}
