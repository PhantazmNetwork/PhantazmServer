package com.github.phantazmnetwork.commons.vector;

/**
 * Contains shared Vec3IPool instances. Not part of the public API.
 */
final class Vec3IPools {
    /**
     * The shared Vec3IPool instance.
     * @apiNote Capable of storing 512 values initially. Once initialized, vectors will persist until
     * program termination. This is expected, and desirable so long as the cache width is small enough that the
     * "unrecoverable" memory is negligible.
     */
    static final Vec3IPool GLOBAL = new CubicVec3IPool(8);
}
