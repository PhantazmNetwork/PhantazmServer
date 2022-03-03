package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Walker {
    @NotNull Iterable<Vec3I> walkVectors();
}
