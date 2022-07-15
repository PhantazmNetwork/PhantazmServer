package com.github.phantazmnetwork.commons.vector;

/**
 * Standard implementation of Vec3D. Not a part of the public API.
 */
class BasicVec3D extends Vec3DBase {
    private final double x;
    private final double y;
    private final double z;

    /**
     * Creates a new instance of this class from the given double triplet.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    BasicVec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }
}
