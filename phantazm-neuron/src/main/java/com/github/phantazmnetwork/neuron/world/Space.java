package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a basic way to interact with "space" containing {@link Solid} instances in various locations.
 */
public interface Space {
    /**
     * Values used to communicate the order by which solids should be iterated. For example, {@link Order#XYZ} signals
     * that {@code x}-values should be iterated first, the {@code y}, followed by {@code z}.
     */
    enum Order {
        /**
         * {@code x}-first, then {@code y}, then {@code z} iteration order.
         */
        XYZ,

        /**
         * {@code y}-first, then {@code x}, then {@code z} iteration order.
         */
        YXZ,

        /**
         * {@code y}-first, then {@code z}, then {@code x} iteration order.
         */
        YZX,

        /**
         * {@code z}-first, then {@code y}, then {@code x} iteration order.
         */
        ZYX,

        /**
         * {@code z}-first, then {@code x}, then {@code y} iteration order.
         */
        ZXY,

        /**
         * {@code x}-first, then {@code z}, then {@code y} iteration order.
         */
        XZY
    }

    @Nullable Solid solidAt(int x, int y, int z);

    @NotNull Iterable<? extends Solid> solidsOverlapping(double oX, double oY, double oZ, double vX, double vY,
                                                         double vZ, @NotNull Order order);
}
