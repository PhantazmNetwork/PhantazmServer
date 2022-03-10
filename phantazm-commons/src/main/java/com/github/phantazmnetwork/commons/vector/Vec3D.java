package com.github.phantazmnetwork.commons.vector;

public interface Vec3D {
    double getX();

    double getY();

    double getZ();

    static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        double dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }
}
