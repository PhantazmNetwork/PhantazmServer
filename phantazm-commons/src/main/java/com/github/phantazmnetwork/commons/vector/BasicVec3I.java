package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Standard implementation of Vec3I. Provides a suitable implementation of {@link Object#equals(Object)} and
 * {@link Object#hashCode()}, so these may be safely used as keys for maps and stored in sets.</p>
 *
 * <p>This class cannot be inherited. It is not part of the public API. Instances can be obtained through the static
 * factory method {@link Vec3I#of(int, int, int)}.</p>
 */
@SuppressWarnings("ClassCanBeRecord")
final class BasicVec3I implements Vec3I, Comparable<Vec3I> {
    private final int x;
    private final int y;
    private final int z;

    /**
     * Creates a new BasicVec3I from the provided integers. It's generally preferable to use
     * {@link Vec3I#of(int, int, int)} to create Vec3I instances because the latter may make use of a constant pool.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     */
    BasicVec3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(obj instanceof Vec3I other) {
            return Vec3I.equals(x, y, z, other.getX(), other.getY(), other.getZ());
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 31 + x;
        result = 31 * result + y;
        return 31 * result + z;
    }

    @Override
    public String toString() {
        return "Vec3I{x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    @Override
    public int compareTo(@NotNull Vec3I o) {
        int xCompare = Integer.compare(x, o.getX());
        if(xCompare == 0) {
            int yCompare = Integer.compare(y, o.getY());
            if(yCompare == 0) {
                return Integer.compare(z, o.getZ());
            }

            return yCompare;
        }

        return xCompare;
    }
}
