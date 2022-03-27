package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import org.jetbrains.annotations.NotNull;

public interface Solid {
    @NotNull Vec3F getMin();

    @NotNull Vec3F getMax();
}
