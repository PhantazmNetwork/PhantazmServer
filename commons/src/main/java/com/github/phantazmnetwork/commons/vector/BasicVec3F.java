package com.github.phantazmnetwork.commons.vector;

/**
 * Standard implementation of Vec3F. Not a part of the public API.
 */
class BasicVec3F extends Vec3FBase {
    private final float x;
    private final float y;
    private final float z;

    /**
     * Creates a new instance of this class from the given float triplet.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    BasicVec3F(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getZ() {
        return z;
    }
}
