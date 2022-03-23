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
        return Math.min(oX1, oX1 + vX1) + Math.min(-oX2, -oX2 - vX2) < 0 &&
                Math.min(oY1, oY1 + vY1) + Math.min(-oY2, -oY2 - vY2) < 0 &&
                Math.min(oZ1, oZ1 + vZ1) + Math.min(-oZ2, -oZ2 - vZ2) < 0 &&
                Math.max(oX1, oX1 + vX1) + Math.max(-oX2, -oX2 - vX2) >= 0 &&
                Math.max(oY1, oY1 + vY1) + Math.max(-oY2, -oY2 - vY2) >= 0 &&
                Math.max(oZ1, oZ1 + vZ1) + Math.max(-oZ2, -oZ2 - vZ2) >= 0;
    }
}
