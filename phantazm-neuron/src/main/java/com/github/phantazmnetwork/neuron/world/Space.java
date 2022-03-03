package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

public interface Space {
    @NotNull Iterable<Bounds> collisionsAt(double minX, double minY, double minZ, double maxX, double maxY,
                                           double maxZ);
}
