package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a 3-dimensional single-precision floating point vector.
 */
public interface Vec3F extends Comparable<Vec3F> {
    /**
     * Represents the origin (0, 0, 0).
     */
    Vec3F ORIGIN = of(0F, 0F, 0F);

    /**
     * Gets the x-component of this vector.
     * @return the x-component of this vector
     */
    float getX();

    /**
     * Gets the y-component of this vector.
     * @return the y-component of this vector
     */
    float getY();

    /**
     * Gets the z-component of this vector.
     * @return the z-component of this vector
     */
    float getZ();

    /**
     * Returns a new implementation of this interface. The returned object will be immutable.
     * @param x the x-component of the new vector
     * @param y the y-component of the new vector
     * @param z the z-component of the new vector
     * @return a new, immutable floating-point vector
     */
    static @NotNull Vec3F of(float x, float y, float z) {
        return new BasicVec3F(x, y, z);
    }

    /**
     * Creates a new Vec3F from casting each {@code double} component to a float.
     * @param x the x-component of the new vector
     * @param y the y-component of the new vector
     * @param z the z-component of the new vector
     * @return a new, immutable floating-point vector
     */
    static @NotNull Vec3F ofDouble(double x, double y, double z) {
        return new BasicVec3F((float) x, (float) y, (float) z);
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
    static boolean equals(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
