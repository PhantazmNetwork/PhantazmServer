package com.github.phantazmnetwork.commons.vector;

public class ImmutableVec3I implements Vec3I {
    private static final int PRIME = 31;

    protected final int x;
    protected final int y;
    protected final int z;

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
        if(obj instanceof ImmutableVec3I other) {
            return other.x == x && other.y == y && other.z == z;
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
