package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Collider {
    boolean collidesAt(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    @NotNull Iterable<? extends Solid> solidsOverlapping(double minX, double minY, double minZ, double maxX,
                                                         double maxY, double maxZ);

    @Nullable Solid findHighest(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    @Nullable Solid findLowest(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
