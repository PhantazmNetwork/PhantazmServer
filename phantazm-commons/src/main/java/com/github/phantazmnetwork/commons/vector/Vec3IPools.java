package com.github.phantazmnetwork.commons.vector;

/**
 * Contains shared Vec3IPool instances. Not part of the public API.
 */
final class Vec3IPools {
    private Vec3IPools() { throw new UnsupportedOperationException(); }

    /**
     * The shared Vec3IPool instance used by {@link Vec3I#of(int, int, int)}.
     * @apiNote Capable of storing 512 values initially. Vector objects will persist until program termination. This is
     * expected, and desirable so long as the cache width is small enough that the "unrecoverable" memory is negligible.
     */
    static final Vec3IPool GLOBAL = new CubicVec3IPool(8);
}
