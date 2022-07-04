package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Contains standard methods common to all {@link Vec3F} implementations.
 */
public abstract class Vec3FBase implements Vec3F {
    @Override
    public int hashCode() {
        return Vec3F.hash(getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj instanceof Vec3F other) {
            return Vec3F.equals(getX(), getY(), getZ(), other.getX(), other.getY(), other.getZ());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Vec3F{x=" + getX() + ", y=" + getY() + ", z=" + getZ() + "}";
    }

    @Override
    public int compareTo(@NotNull Vec3F o) {
        int xCompare = Float.compare(getX(), o.getX());
        if (xCompare == 0) {
            int yCompare = Float.compare(getY(), o.getY());
            if (yCompare == 0) {
                return Float.compare(getZ(), o.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }
}
