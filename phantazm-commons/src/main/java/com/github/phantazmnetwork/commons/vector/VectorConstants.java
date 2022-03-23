package com.github.phantazmnetwork.commons.vector;

/**
 * Stores useful constants related to vectors.
 */
public final class VectorConstants {
    /**
     * The maximum distance apart any pair of components of two vectors may differ in order to still be considered
     * "equal" by methods like {@link Vec3D#fuzzyEquals(double, double, double, double, double, double)}.
     */
    public static final double EPSILON = 0.00001;
}
