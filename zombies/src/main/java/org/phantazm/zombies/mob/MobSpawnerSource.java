package org.phantazm.zombies.mob;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.function.Supplier;

public interface MobSpawnerSource {
    @NotNull MobSpawner make(@NotNull Supplier<? extends MapObjects> mapObjects, @NotNull MobStore mobStore);
}
