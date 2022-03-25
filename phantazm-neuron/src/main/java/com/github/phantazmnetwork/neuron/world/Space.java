package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.Vec3IFunction;
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
        XYZ((f, s, t, space) -> space.solidAt(f, s, t)),

        /**
         * {@code y}-first, then {@code x}, then {@code z} iteration order.
         */
        YXZ((f, s, t, space) -> space.solidAt(s, f, t)),

        /**
         * {@code y}-first, then {@code z}, then {@code x} iteration order.
         */
        YZX((f, s, t, space) -> space.solidAt(t, f, s)),

        /**
         * {@code z}-first, then {@code y}, then {@code x} iteration order.
         */
        ZYX((f, s, t, space) -> space.solidAt(t, s, f)),

        /**
         * {@code z}-first, then {@code x}, then {@code y} iteration order.
         */
        ZXY((f, s, t, space) -> space.solidAt(s, t, f)),

        /**
         * {@code x}-first, then {@code z}, then {@code y} iteration order.
         */
        XZY((f, s, t, space) -> space.solidAt(f, t, s));

        @FunctionalInterface
        interface SpaceAccessor {
            @Nullable Solid getSolid(int x, int y, int z, Space space);
        }

        record IterationVariables(int getFirstOrigin, int getSecondOrigin, int getThirdOrigin, int getFirstIncrement,
                                  int getSecondIncrement, int getThirdIncrement, int getFirstEnd, int getSecondEnd,
                                  int getThirdEnd) { }

        private final SpaceAccessor accessor;

        Order(@NotNull SpaceAccessor accessor) {
            this.accessor = accessor;
        }

        @NotNull SpaceAccessor getAccessor() {
            return accessor;
        }

        @SuppressWarnings("DuplicatedCode") //this is an unintelliJ moment
        @NotNull IterationVariables computeVariables(int xOrg, int yOrg, int zOrg, int xInc, int yInc,
                                                     int zInc, int xEnd, int yEnd, int zEnd) {
            return switch (this) {
                case XYZ -> new IterationVariables(xOrg, yOrg, zOrg, xInc, yInc, zInc, xEnd, yEnd, zEnd);
                case YXZ -> new IterationVariables(yOrg, xOrg, zOrg, yInc, xInc, zInc, yEnd, xEnd, zEnd);
                case YZX -> new IterationVariables(yOrg, zOrg, xOrg, yInc, zInc, xInc, yEnd, zEnd, xEnd);
                case ZYX -> new IterationVariables(zOrg, yOrg, xOrg, zInc, yInc, xInc, zEnd, yEnd, xEnd);
                case ZXY -> new IterationVariables(zOrg, xOrg, yOrg, zInc, xInc, yInc, zEnd, xEnd, yEnd);
                case XZY -> new IterationVariables(xOrg, zOrg, yOrg, xInc, zInc, yInc, xEnd, zEnd, yEnd);
            };
        }
    }

    @Nullable Solid solidAt(int x, int y, int z);

    @NotNull Iterable<? extends Solid> solidsOverlapping(double oX, double oY, double oZ, double vX, double vY,
                                                         double vZ, @NotNull Order order);
}
