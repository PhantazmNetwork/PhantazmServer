package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Represents an <i>immutable</i> 3D integer vector (point). Contains some utility methods for working with
 * vectors/points.</p>
 *
 * <p>Implementations must provide suitable overrides for {@link Object#equals(Object)} and {@link Object#hashCode()},
 * based only off of the vector's component values. Any two Vec3I instances must be considered equal if and only if each
 * of their components is equal.</p>
 */
public interface Vec3I {
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
     * Creates a new, immutable Vec3I implementation. This may not always create a new vector; common values may (but
     * are not guaranteed to be) returned from a persistent cache.
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
        double dX = x1 - x2;
        double dY = y1 - y2;
        double dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }

    /**
     * <p>Computes the <i>Manhattan distance</i> between two points (x1, y1, z1) and (x2, y2, z2). This is defined as
     * the sum of the absolute value of the differences between each component.</p>
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return the Manhattan distance between the point {@code first} and the point {@code second}
     */
    static double manhattanDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
    }

    /**
     * Determines if two points are equal. Equivalent to <code>x1 == x2 && y1 == y2 && z1 == z2</code>.
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
