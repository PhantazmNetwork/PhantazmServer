package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * A mutable implementation of Vec3I. Not part of the public API.
 */
class MutableVec3I extends Vec3IBase {
    private int x;
    private int y;
    private int z;

    /**
     * Creates a new, mutable vector.
     *
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     */
    MutableVec3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public @NotNull Vec3I set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public @NotNull Vec3I op(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }
}
