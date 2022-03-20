package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Solid {
    @NotNull Vec3I getPosition();

    float originX();

    float originY();

    float originZ();

    float vectorX();

    float vectorY();

    float vectorZ();

    static boolean overlaps(double oX1, double oY1, double oZ1, double vX1, double vY1, double vZ1, double oX2,
                            double oY2, double oZ2, double vX2, double vY2, double vZ2) {
        return oX1 < oX2 + vX2 && oX1 + vX1 > oX2 && oY1 < oY2 + vY2 && oY1 + vY1 > oY2 && oZ1 < oZ2 + vZ2 && oZ1 +
                vZ1 > oZ2;
    }
}
