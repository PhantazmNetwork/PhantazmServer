package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Standard implementation of Vec3D. Not a part of the public API. Instances are compared lexicographically, first by
 * {@code x} value, then {@code y} value, then {@code z} value. This object's {@link Vec3D#compareTo(Object)} method is
 * <i>consistent</i> with {@link Object#equals(Object)}.
 */
final record BasicVec3D(double getX, double getY, double getZ) implements Vec3D {
    /**
     * Creates a new instance of this record. {@link Vec3D#of(double, double, double)} should be used in preference to
     * this constructor.
     * @param getX the x-component
     * @param getY the y-component
     * @param getZ the z-component
     */
    BasicVec3D {}

    @Override
    public int compareTo(@NotNull Vec3D o) {
        int xCompare = Double.compare(getX, o.getX());
        if (xCompare == 0) {
            int yCompare = Double.compare(getY, o.getY());
            if (yCompare == 0) {
                return Double.compare(getZ, o.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }

    @Override
    public String toString() {
        return "Vec3D[x=" + getX + ", y=" + getY + ", z=" + getZ + "]";
    }
}
