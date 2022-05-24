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
        XYZ((f, s, t, space) -> space.solidAt(f, s, t), IterationVariables::new),

        /**
         * {@code y}-first, then {@code x}, then {@code z} iteration order.
         */
        YXZ((f, s, t, space) -> space.solidAt(s, f, t), (int xOrg, int yOrg, int zOrg, int xInc, int yInc, int zInc,
                                                         int xEnd, int yEnd, int zEnd) -> new IterationVariables(yOrg,
                xOrg, zOrg, yInc, xInc, zInc, yEnd, xEnd, zEnd)),

        /**
         * {@code y}-first, then {@code z}, then {@code x} iteration order.
         */
        YZX((f, s, t, space) -> space.solidAt(t, f, s), (int xOrg, int yOrg, int zOrg, int xInc, int yInc, int zInc,
                                                         int xEnd, int yEnd, int zEnd) -> new IterationVariables(yOrg,
                zOrg, xOrg, yInc, zInc, xInc, yEnd, zEnd, xEnd)),

        /**
         * {@code z}-first, then {@code y}, then {@code x} iteration order.
         */
        ZYX((f, s, t, space) -> space.solidAt(t, s, f), (int xOrg, int yOrg, int zOrg, int xInc, int yInc, int zInc,
                                                         int xEnd, int yEnd, int zEnd) -> new IterationVariables(zOrg,
                yOrg, xOrg, zInc, yInc, xInc, zEnd, yEnd, xEnd)),

        /**
         * {@code z}-first, then {@code x}, then {@code y} iteration order.
         */
        ZXY((f, s, t, space) -> space.solidAt(s, t, f), (int xOrg, int yOrg, int zOrg, int xInc, int yInc, int zInc,
                                                         int xEnd, int yEnd, int zEnd) -> new IterationVariables(zOrg,
                xOrg, yOrg, zInc, xInc, yInc, zEnd, xEnd, yEnd)),

        /**
         * {@code x}-first, then {@code z}, then {@code y} iteration order.
         */
        XZY((f, s, t, space) -> space.solidAt(f, t, s), (int xOrg, int yOrg, int zOrg, int xInc, int yInc, int zInc,
                                                         int xEnd, int yEnd, int zEnd) -> new IterationVariables(xOrg,
                zOrg, yOrg, xInc, zInc, yInc, xEnd, zEnd, yEnd));

        /**
         * Represents a function that retrieves a {@link Solid} from a {@link Space} instance.
         */
        @FunctionalInterface
        public interface SpaceAccessor {
            /**
             * Retrieves the solid that may be located at the given vector from a {@link Space} instance.
             * @param x the x-coordinate of the solid
             * @param y the y-coordinate of the solid
             * @param z the z-coordinate of the solid
             * @param space the Space instance used to retrieve the solid
             * @return the solid instance located at the provided vector, or null if none exists at that location
             */
            @Nullable Solid getSolid(int x, int y, int z, Space space);
        }

        /**
         * Represents a function that may compute an {@link IterationVariables} instance from the provided values.
         */
        @FunctionalInterface
        public interface IterationVariablesSupplier {
            /**
             * Computes an {@link IterationVariables} instance, given the initialization values.
             * @param xOrg the x-component of the starting vector
             * @param yOrg the y-component of the starting vector
             * @param zOrg the z-component of the starting vector
             * @param xInc the x-increment; can be -1, 0, or 1
             * @param yInc the y-increment; can be -1, 0, or 1
             * @param zInc the z-increment; can be -1, 0, or 1
             * @param xEnd the x-component of the ending vector
             * @param yEnd the y-component of the ending vector
             * @param zEnd the z-component of the ending vector
             * @return a new IterationVariables instance
             */
            @NotNull IterationVariables make(int xOrg, int yOrg, int zOrg, int xInc, int yInc, int zInc, int xEnd,
                                             int yEnd, int zEnd);
        }

        /**
         * A record containing the immutable variables which dictate iteration behavior.
         */
        public record IterationVariables(int getFirstOrigin, int getSecondOrigin, int getThirdOrigin,
                                         int getFirstIncrement, int getSecondIncrement, int getThirdIncrement,
                                         int getFirstEnd, int getSecondEnd, int getThirdEnd) {
            /**
             * Creates a new instance of IterationVariables.
             * @param getFirstOrigin the start of the first component to iterate
             * @param getSecondOrigin the start of the second component to iterate
             * @param getThirdOrigin the start of the third component to iterate
             * @param getFirstIncrement the increment of the first component
             * @param getSecondIncrement the increment of the second component
             * @param getThirdIncrement the increment of the third component
             * @param getFirstEnd the end of the first component to iterate
             * @param getSecondEnd the end of the second component to iterate
             * @param getThirdEnd the end of the third component to iterate
             */
            public IterationVariables {}
        }

        private final SpaceAccessor accessor;
        private final IterationVariablesSupplier variablesSupplier;

        /**
         * The standard enum constructor, taking a {@link SpaceAccessor} and {@link IterationVariablesSupplier}.
         * @param accessor the SpaceAccessor instance used for this constant
         * @param variablesSupplier the IterationVariablesSupplier used for this constant
         */
        Order(@NotNull SpaceAccessor accessor, @NotNull IterationVariablesSupplier variablesSupplier) {
            this.accessor = accessor;
            this.variablesSupplier = variablesSupplier;
        }

        /**
         * Returns the {@link SpaceAccessor} for this constant.
         * @return the SpaceAccessor for this constant
         */
        public @NotNull SpaceAccessor getSpaceAccessor() {
            return accessor;
        }

        /**
         * Returns the {@link IterationVariablesSupplier} for this constant.
         * @return the IterationVariablesSupplier for this constant
         */
        public @NotNull IterationVariablesSupplier getVariablesSupplier() {
            return variablesSupplier;
        }
    }

    /**
     * Returns the solid present at (x, y, z), or null if there are none
     * @param x the x-coordinate of the solid
     * @param y the y-coordinate of the solid
     * @param z the z-coordinate of the solid
     * @return the solid present at (x, y, z), or null if there are none
     */
    @Nullable Solid solidAt(int x, int y, int z);

    /**
     * Returns a {@link SolidSource} over the provided origin-vector AABB, using the specified {@link Order}. The
     * iterable will iterate all solids in the region encompassed by the AABB, starting at the origin vector. See
     * {@link Collider} for more information about the origin-vector form of AABBs.
     * @param oX the origin x
     * @param oY the origin y
     * @param oZ the origin z
     * @param vX the vector x
     * @param vY the vector y
     * @param vZ the vector z
     * @param order the iteration order used
     * @return a SolidIterable which will iterate all solids present in the AABB
     */
    @NotNull SolidSource solidsOverlapping(double oX, double oY, double oZ, double vX, double vY, double vZ,
                                           @NotNull Order order);
}
