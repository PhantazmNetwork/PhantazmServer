package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Standard implementation of Vec3F. Not a part of the public API. Instances are compared lexicographically, first by
 * {@code x} value, then {@code y} value, then {@code z} value. This object's {@link Vec3F#compareTo(Object)} method is
 * <i>consistent</i> with {@link Object#equals(Object)}.
 */
record BasicVec3F(float getX, float getY, float getZ) implements Vec3F {
    /**
     * Creates a new instance of this record. {@link Vec3F#of(float, float, float)} should be used in preference to this
     * constructor.
     * @param getX the x-component
     * @param getY the y-component
     * @param getZ the z-component
     */
    BasicVec3F {}

    @Override
    public int compareTo(@NotNull Vec3F o) {
        int xCompare = Float.compare(getX, o.getX());
        if (xCompare == 0) {
            int yCompare = Float.compare(getY, o.getY());
            if (yCompare == 0) {
                return Float.compare(getZ, o.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }

    @Override
    public String toString() {
        return "Vec3F[x=" + getX + ", y=" + getY + ", z=" + getZ + "]";
    }
}
