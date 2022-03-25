package com.github.phantazmnetwork.commons.vector;

@SuppressWarnings("ClassCanBeRecord")
public final class BasicVec3D implements Vec3D {
    private final double x;
    private final double y;
    private final double z;

    public BasicVec3D(double x, double y, double z) {
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

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(obj instanceof Vec3D other) {
            return Vec3D.equals(x, y, z, other.getX(), other.getY(), other.getZ());
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (Double.doubleToLongBits(x) ^ (Double.doubleToLongBits(x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(y) ^ (Double.doubleToLongBits(y) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(z) ^ (Double.doubleToLongBits(z) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "Vec3D{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
