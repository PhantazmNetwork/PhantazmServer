package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Represents an <i>immutable</i> 3D integer vector (point). Contains some utility methods for working with
 * vectors/points.</p>
 *
 * <p>Implementations must provide suitable overrides for {@link Object#equals(Object)} and {@link Object#hashCode()},
 * based only off of the vector's component values. Any two Vec3I instances must be considered equal if and only if each
 * of their components is equal. This allows for different Vec3I implementations to be compared against each other, and
 * ensures consistency with static methods like {@link Vec3I#equals(int, int, int, int, int, int)}.</p>
 */
public interface Vec3I extends Comparable<Vec3I> {
    /**
     * The x-component of this vector.
     * @return the x-component
     */
    int getX();

    /**
     * The y-component of this vector.
     * @return the y-component
     */
    int getY();

    /**
     * The z-component of this vector.
     * @return the z-component
     */
    int getZ();

    /**
     * A static, immutable vector representing the origin (0, 0, 0).
     */
    Vec3I ORIGIN = of(0, 0, 0);

    /**
     * Returns a Vec3I created from taking the mathematical floor of each given {@code double} component before casting
     * to an integer.
     * @param x the x-component of the new vector, which will be floored
     * @param y the y-component of the new vector, which will be floored
     * @param z the z-component of the new vector, which will be floored
     * @return the floored vector
     */
    static @NotNull Vec3I floored(double x, double y, double z) {
        return of((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
    }

    /**
     * <p>Returns an immutable Vec3I implementation. This may not always create a new vector; common values may (but are
     * not guaranteed to be) returned from a persistent cache.</p>
     *
     * @apiNote This method is threadsafe, as it relies only on the global {@link Vec3IPool} instance to supply cached
     * values.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return a new, immutable Vec3I instance with the given components
     * @see BasicVec3I
     */
    static @NotNull Vec3I of(int x, int y, int z) {
        Vec3I cached = Vec3IPools.GLOBAL.fromCache(x, y, z);
        if(cached != null) {
            return cached;
        }

        return new BasicVec3I(x, y, z);
    }

    /**
     * Computes the hashcode of an integer triplet.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the hash of the three integers
     */
    static int hash(int x, int y, int z) {
        int h = 7;
        h = 31 * h + x;
        h = 31 * h + y;
        return 31 * h + z;
    }

    /**
     * Compares two Vec3I instances, first by x-value, then y-value, then z-value. This comparison function is
     * consistent with equals.
     * @param first the first vector
     * @param second the second vector
     * @return -1 if first &lt; second, 0 if first == second, 1 if first &gt; second
     */
    static int compare(@NotNull Vec3I first, @NotNull Vec3I second) {
        int xCompare = Integer.compare(first.getX(), second.getX());
        if (xCompare == 0) {
            int yCompare = Integer.compare(first.getY(), second.getY());
            if (yCompare == 0) {
                return Integer.compare(first.getZ(), second.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }

    /**
     * <p>Computes the <i>squared distance</i> between two points (x1, y1, z1) and (x2, y2, z2). This uses the standard
     * distance formula for two 3-dimensional points, but omits the square root for performance.</p>
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return the squared distance between the point {@code first} and the point {@code second}
     */
    static double squaredDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dX = x1 - x2;
        int dY = y1 - y2;
        int dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }

    /**
     * Determines if two points are equal.
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return true if the two points are the same, false otherwise
     */
    static boolean equals(int x1, int y1, int z1, int x2, int y2, int z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
