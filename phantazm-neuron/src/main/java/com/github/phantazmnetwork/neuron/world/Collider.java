package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

public interface Collider {
    enum Order {
        XYZ,
        YXZ,
        YZX,
        ZYX,
        ZXY,
        XZY
    }

    boolean collidesAt(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                       @NotNull Order order);

    @NotNull Iterable<? extends Solid> collisionsMovingAlong(double minX, double minY, double minZ, double maxX,
                                                             double maxY, double maxZ, int deltaX, int deltaY,
                                                             int deltaZ, @NotNull Order order);

    boolean collidesMovingAlong(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                int deltaX, int deltaY, int deltaZ, @NotNull Order order);
}
