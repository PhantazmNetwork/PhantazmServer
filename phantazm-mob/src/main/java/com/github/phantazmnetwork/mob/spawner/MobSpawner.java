package com.github.phantazmnetwork.mob.spawner;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface MobSpawner {

    @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Point point, @NotNull MobModel model);

}
