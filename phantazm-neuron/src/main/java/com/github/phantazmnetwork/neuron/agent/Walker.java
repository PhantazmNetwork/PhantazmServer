package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Walker {
    @NotNull Iterable<Vec3I> walkVectors(int x, int y, int z);
}
