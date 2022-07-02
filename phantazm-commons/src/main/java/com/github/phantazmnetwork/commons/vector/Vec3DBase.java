package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Contains standard methods common to all {@link Vec3D} implementations.
 */
public abstract class Vec3DBase implements Vec3D {
    @Override
    public int hashCode() {
        return Vec3D.hash(getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj instanceof Vec3D other) {
            return Vec3D.equals(getX(), getY(), getZ(), other.getX(), other.getY(), other.getZ());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Vec3D{x=" + getX() + ", y=" + getY() + ", z=" + getZ() + "}";
    }

    @Override
    public int compareTo(@NotNull Vec3D other) {
        int xCompare = Double.compare(getX(), other.getX());
        if (xCompare == 0) {
            int yCompare = Double.compare(getY(), other.getY());
            if (yCompare == 0) {
                return Double.compare(getZ(), other.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }
}
