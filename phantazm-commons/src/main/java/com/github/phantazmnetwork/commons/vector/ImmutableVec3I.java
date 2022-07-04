package com.github.phantazmnetwork.commons.vector;

/**
 * Standard, immutable implementation of Vec3I. Not a part of the public API.
 */
class ImmutableVec3I extends Vec3IBase {
    private final int x;
    private final int y;
    private final int z;

    /**
     * Creates a new instance of this class from the given int triplet.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    ImmutableVec3I(int x, int y, int z) {
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
}
