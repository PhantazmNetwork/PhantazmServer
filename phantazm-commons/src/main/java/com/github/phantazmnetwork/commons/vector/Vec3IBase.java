package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Contains standard methods common to all {@link Vec3I} implementations.
 */
public abstract class Vec3IBase implements Vec3I {
    @Override
    public int hashCode() {
        return Vec3I.hash(getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj instanceof Vec3I other) {
            return Vec3I.equals(getX(), getY(), getZ(), other.getX(), other.getY(), other.getZ());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Vec3I{x=" + getX() + ", y=" + getY() + ", z=" + getZ() + "}";
    }

    @Override
    public int compareTo(@NotNull Vec3I o) {
        int xCompare = Integer.compare(getX(), o.getX());
        if (xCompare == 0) {
            int yCompare = Integer.compare(getY(), o.getY());
            if (yCompare == 0) {
                return Integer.compare(getZ(), o.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }
}
