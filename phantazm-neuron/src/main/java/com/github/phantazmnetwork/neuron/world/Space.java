package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a basic way to interact with "space" containing {@link Solid} instances in various locations.
 */
public interface Space {
    enum Order {
        XYZ,
        YXZ,
        YZX,
        ZYX,
        ZXY,
        XZY
    }

    @Nullable Solid solidAt(int x, int y, int z);

    @NotNull Iterable<? extends Solid> solidsOverlapping(double oX, double oY, double oZ, double vX, double vY,
                                                         double vZ, @NotNull Order order);
}
