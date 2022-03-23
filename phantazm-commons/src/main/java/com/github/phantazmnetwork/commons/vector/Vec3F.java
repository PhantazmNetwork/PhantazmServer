package com.github.phantazmnetwork.commons.vector;

public interface Vec3F {
    float getX();

    float getY();

    float getZ();

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
    static boolean equals(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
