package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.Nullable;

/**
 * <p>Holds a cache of common immutable vectors. Used internally to reduce memory and allocation overhead.</p>
 *
 * <p>This class stores exactly 512 {@link ImmutableVec3I} instances. These vectors correspond to a "cube" starting at
 * (-4, -4, -4) with dimensions (8, 8, 8).</p>
 */
final class Vec3IPool {
    /**
     * The width of the cache. Must be a power of 2.
     */
    static final int CACHE_WIDTH = 8;

    private static final Vec3I[] CACHE = new Vec3I[CACHE_WIDTH * CACHE_WIDTH * CACHE_WIDTH];
    private static final int HALF_CACHE_WIDTH;
    private static final int CACHE_SHIFT_Y;
    private static final int CACHE_SHIFT_Z;

    static {
        HALF_CACHE_WIDTH = CACHE_WIDTH >> 1;

        //fast way to compute log base 2 of base-2 number
        CACHE_SHIFT_Y = Integer.numberOfTrailingZeros(CACHE_WIDTH);
        CACHE_SHIFT_Z = CACHE_SHIFT_Y << 1;

        int beginInclusive = -HALF_CACHE_WIDTH;
        int endExclusive = HALF_CACHE_WIDTH;

        int pos = 0;
        //initialize the cache values
        for(int i = beginInclusive; i < endExclusive; i++) {
            for(int j = beginInclusive; j < endExclusive; j++) {
                for(int k = beginInclusive; k < endExclusive; k++) {
                    CACHE[pos++] = new ImmutableVec3I(i, j, k);
                }
            }
        }
    }

    /**
     * Attempts to retrieve a vector from this cache. If the value is cached, it is returned. Otherwise, this method
     * will return null.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return the Vec3I instance if it's cached, or null otherwise
     */
    static @Nullable Vec3I retrieve(int x, int y, int z) {
        if(inRange(x) && inRange(y) && inRange(z)) {
            return CACHE[((x + HALF_CACHE_WIDTH) << CACHE_SHIFT_Z) | ((y + HALF_CACHE_WIDTH) << CACHE_SHIFT_Y) | (z +
                    HALF_CACHE_WIDTH)];
        }

        return null;
    }

    private static boolean inRange(int value) {
        return value >= -HALF_CACHE_WIDTH && value < HALF_CACHE_WIDTH;
    }
}
