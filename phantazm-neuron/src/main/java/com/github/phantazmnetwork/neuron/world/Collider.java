package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

public interface Collider {
    boolean collidesAt(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    @NotNull Iterable<? extends Solid> solidsAt(double minX, double minY, double minZ, double maxX, double maxY,
                                                double maxZ);
}
