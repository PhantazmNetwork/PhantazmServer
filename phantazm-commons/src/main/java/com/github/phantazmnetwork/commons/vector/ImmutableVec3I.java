package com.github.phantazmnetwork.commons.vector;

/**
 * Standard, immutable implementation of Vec3I. Provides a suitable implementation of {@link Object#equals(Object)} and
 * {@link Object#hashCode()}, so these may be safely used as keys for maps and stored in sets.
 */
public class ImmutableVec3I implements Vec3I {
    private static final int PRIME = 31;

    protected final int x;
    protected final int y;
    protected final int z;

    /**
     * Creates a new ImmutableVec3I from the provided integers. This constructor is public, but it's generally
     * preferable to use {@link Vec3I#of(int, int, int)} to create instances of this class.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     */
    public ImmutableVec3I(int x, int y, int z) {
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
        if(obj instanceof Vec3I other) {
            return other.getX() == x && other.getY() == y && other.getZ() == z;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = PRIME + x;
        result = PRIME * result + y;
        return PRIME * result + z;
    }

    @Override
    public String toString() {
        return "Vec3I{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
