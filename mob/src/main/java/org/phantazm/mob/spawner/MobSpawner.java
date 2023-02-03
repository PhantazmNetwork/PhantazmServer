package org.phantazm.mob.spawner;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.Spawner;

import java.util.UUID;

/**
 * A spawner for {@link PhantazmMob}s.
 * While {@link Spawner} spawns {@link ProximaEntity}s, this spawns {@link PhantazmMob}s.
 */
@FunctionalInterface
public interface MobSpawner {

    /**
     * Spawns a {@link PhantazmMob}.
     *
     * @param instance The {@link Instance} to spawn the {@link PhantazmMob} in
     * @param point    The {@link Pos} to spawn the {@link PhantazmMob} at
     * @param model    The {@link MobModel} of the {@link PhantazmMob} to spawn
     * @return A new {@link PhantazmMob}
     */
    @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Pos point, @NotNull MobStore mobStore,
            @NotNull MobModel model);
}
