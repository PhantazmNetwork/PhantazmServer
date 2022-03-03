package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.neuron.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Space {
    @NotNull Iterable<Bounds> collisionsIn(@NotNull Vec3I min, @NotNull Vec3I max);
}
