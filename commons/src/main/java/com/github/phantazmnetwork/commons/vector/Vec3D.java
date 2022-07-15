package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a 3-dimensional double-precision floating point vector.
 */
public interface Vec3D extends Comparable<Vec3D> {
    /**
     * Represents the origin (0, 0, 0).
     */
    Vec3D ORIGIN = new BasicVec3D(0, 0, 0);

    /**
     * Returns a new implementation of this interface. The returned object will be immutable.
     *
     * @param x the x-component of the new vector
     * @param y the y-component of the new vector
     * @param z the z-component of the new vector
     * @return a new, immutable vector
     */
    static @NotNull Vec3D of(double x, double y, double z) {
        return new BasicVec3D(x, y, z);
    }

    /**
     * Creates a new, immutable Vec3D from the provided integer vector.
     *
     * @param vec the integer vector
     * @return a new double vector
     */
    static @NotNull Vec3D of(@NotNull Vec3I vec) {
        return of(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Determines if two points are equal.
     *
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return true if the two points are the same, false otherwise
     */
    static boolean equals(double x1, double y1, double z1, double x2, double y2, double z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }

    /**
     * Computes the hashcode of a double triplet.
     *
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the hash of the three integers
     */
    static int hash(double x, double y, double z) {
        int h = 7;
        h = 31 * h + Double.hashCode(x);
        h = 31 * h + Double.hashCode(y);
        return 31 * h + Double.hashCode(z);
    }

    /**
     * <p>Computes the <i>squared distance</i> between two points (x1, y1, z1) and (x2, y2, z2). This uses the standard
     * distance formula for two 3-dimensional points, but omits the square root for performance.</p>
     *
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return the squared distance between the point {@code first} and the point {@code second}
     */
    static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        double dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }

    /**
     * <p>Computes the <i>distance</i> between two points (x1, y1, z1) and (x2, y2, z2). This uses the standard distance
     * formula for two 3-dimensional points.</p>
     *
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return the squared distance between the point {@code first} and the point {@code second}
     */
    static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Gets the x-component of this vector.
     *
     * @return the x-component of this vector
     */
    double getX();

    /**
     * Gets the y-component of this vector.
     *
     * @return the y-component of this vector
     */
    double getY();

    /**
     * Gets the z-component of this vector.
     *
     * @return the z-component of this vector
     */
    double getZ();

    default double squaredDistance(double x, double y, double z) {
        return Vec3D.squaredDistance(x, y, z, getX(), getY(), getZ());
    }

    default double squaredDistance(@NotNull Vec3D other) {
        return squaredDistance(other.getX(), other.getY(), other.getZ());
    }
}
