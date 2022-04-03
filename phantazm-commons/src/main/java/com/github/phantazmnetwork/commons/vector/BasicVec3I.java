package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Standard implementation of Vec3I. Not a part of the public API. Instances are compared lexicographically, first by
 * {@code x} value, then {@code y} value, then {@code z} value. This object's {@link Vec3I#compareTo(Object)} method is
 * <i>consistent</i> with {@link Object#equals(Object)}.
 */
record BasicVec3I(int getX, int getY, int getZ) implements Vec3I {
    /**
     * Creates a new instance of this record. {@link Vec3I#of(int, int, int)} should be used in preference to this
     * constructor.
     * @param getX the x-component
     * @param getY the y-component
     * @param getZ the z-component
     */
    BasicVec3I {}

    @Override
    public int compareTo(@NotNull Vec3I o) {
        int xCompare = Integer.compare(getX, o.getX());
        if (xCompare == 0) {
            int yCompare = Integer.compare(getY, o.getY());
            if (yCompare == 0) {
                return Integer.compare(getZ, o.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }

    @Override
    public String toString() {
        return "Vec3I[x=" + getX + ", y=" + getY + ", z=" + getZ + "]";
    }
}
