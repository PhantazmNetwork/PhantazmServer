package com.github.phantazmnetwork.commons.vector;

public interface Vec3D {
    double getX();

    double getY();

    double getZ();

    /**
     * Determines if two points are equal. Equivalent to <code>x1 == x2 && y1 == y2 && z1 == z2</code>.
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return true if the two points are the same, false otherwise
     */
    static boolean equals(double x1, double y1, double z1, double x2, double y2, double z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }

    static boolean fuzzyEquals(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.abs(x1 - x2) < VectorConstants.EPSILON && Math.abs(y1 - y2) < VectorConstants.EPSILON &&
                Math.abs(z1 - z2) < VectorConstants.EPSILON;

    }
}
