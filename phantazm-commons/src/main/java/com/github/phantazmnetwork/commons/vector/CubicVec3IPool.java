package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.Nullable;

/**
 * A "cubical" cache of immutable Vec3I instances. Cached values are stored in an array. Values are lazily initialized.
 * Initialized vectors are instances of {@link BasicVec3I}.
 * @see BasicVec3I
 */
final class CubicVec3IPool implements Vec3IPool {
    private final Vec3I[] cache;
    private final int halfCacheWidth;
    private final int shiftY;
    private final int shiftZ;

    /**
     * Creates a new cache with the specified width. The cache will hold {@code width^3} values, with an even
     * distribution of positive and negative values.
     * @param cacheWidth the width of the cache
     * @throws IllegalArgumentException if cacheWidth is smaller than 2, not a power of 2, or larger than 256
     */
    CubicVec3IPool(int cacheWidth) {
        if(cacheWidth < 2 || !isPowerOf2(cacheWidth)) {
            throw new IllegalArgumentException("cacheWidth must be power of 2 and >= 2");
        }

        //OOM prevention
        if(cacheWidth > 256) {
            throw new IllegalArgumentException("cacheWidth must not be larger than 256");
        }

        this.cache = new Vec3I[cacheWidth * cacheWidth * cacheWidth];
        this.halfCacheWidth = cacheWidth >> 1;

        //fast way to compute log base 2 of base-2 number
        this.shiftY = Integer.numberOfTrailingZeros(cacheWidth);
        this.shiftZ = shiftY << 1;
    }

    private static boolean isPowerOf2(int value) {
        return (value & (value - 1)) == 0;
    }

    private boolean inRange(int value) {
        return value >= -halfCacheWidth && value < halfCacheWidth;
    }

    @Override
    public @Nullable Vec3I fromCache(int x, int y, int z) {
        if(inRange(x) && inRange(y) && inRange(z)) {
            int i = ((x + halfCacheWidth) << shiftZ) | ((y + halfCacheWidth) << shiftY) | (z + halfCacheWidth);
            Vec3I cached = cache[i];
            if(cached == null) {
                cache[i] = cached = new BasicVec3I(x, y, z);
            }

            return cached;
        }

        return null;
    }
}
