package com.github.phantazmnetwork.commons.vector;

public interface Vec3I {
    int getX();

    int getY();

    int getZ();

    Vec3I ORIGIN = new Vec3I() {
        @Override
        public int getX() {
            return 0;
        }

        @Override
        public int getY() {
            return 0;
        }

        @Override
        public int getZ() {
            return 0;
        }
    };

    static int squaredDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dX = x1 - x2;
        int dY = y1 - y2;
        int dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }

    static int manhattanDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
    }

    static boolean equals(int x1, int y1, int z1, int x2, int y2, int z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
