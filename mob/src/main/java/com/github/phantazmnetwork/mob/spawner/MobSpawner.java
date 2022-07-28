package com.github.phantazmnetwork.mob.spawner;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * A spawner for {@link PhantazmMob}s.
 * While {@link Spawner} spawns {@link NeuralEntity}s, this spawns {@link PhantazmMob}s.
 */
@FunctionalInterface
public interface MobSpawner {

    /**
     * Spawns a {@link PhantazmMob}.
     *
     * @param instance The {@link Instance} to spawn the {@link PhantazmMob} in
     * @param point    The {@link Point} to spawn the {@link PhantazmMob} at
     * @param model    The {@link MobModel} of the {@link PhantazmMob} to spawn
     * @return A new {@link PhantazmMob}
     */
    @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Point point, @NotNull MobModel model);

}
