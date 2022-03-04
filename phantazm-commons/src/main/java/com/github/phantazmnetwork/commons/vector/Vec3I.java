package com.github.phantazmnetwork.commons.vector;

public interface Vec3I {
    int getX();

    int getY();

    int getZ();

    static float distanceSquared(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dX = x1 - x2;
        int dY = y1 - y2;
        int dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }
}
