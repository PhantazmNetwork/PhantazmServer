package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.Nullable;

/**
 * <p>A "cubical" cache of immutable Vec3I instances. Cached values are stored in an array. Values are lazily
 * initialized. Initialized vectors are instances of {@link BasicVec3I}.</p>
 *
 * <p>Since an array is initialized with capacity sufficient to hold all possible values for any given width, it is
 * important to avoid allocating a very large pool, as there will be a significant portion of wasted memory in many
 * cases.</p>
 *
 * <p>{@link CubicVec3IPool#fromCache(int, int, int)} is inherently thread-safe.</p>
 * @see BasicVec3I
 */
class CubicVec3IPool implements Vec3IPool {
    private final Vec3I[] cache;
    private final int halfWidth;
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

        //assume we'll need the whole cache
        this.cache = new Vec3I[cacheWidth * cacheWidth * cacheWidth];
        this.halfWidth = cacheWidth >> 1;

        this.shiftY = Integer.numberOfTrailingZeros(cacheWidth);
        this.shiftZ = shiftY << 1;

        //fill the entire cache: this allows fromCache to be made threadsafe without needing to synchronize
        int pos = 0;
        for(int i = 0; i < cacheWidth; i++) {
            for(int j = 0; j < cacheWidth; j++) {
                for(int k = 0; k < cacheWidth; k++) {
                    cache[pos++] = new BasicVec3I(i - halfWidth, j - halfWidth, k - halfWidth);
                }
            }
        }
    }

    private static boolean isPowerOf2(int value) {
        return (value & (value - 1)) == 0;
    }

    private boolean inRange(int value) {
        return value >= -halfWidth && value < halfWidth;
    }

    @Override
    public @Nullable Vec3I fromCache(int x, int y, int z) {
        if(inRange(x) && inRange(y) && inRange(z)) {
            return cache[((x + halfWidth) << shiftZ) | ((y + halfWidth) << shiftY) | (z + halfWidth)];
        }

        return null;
    }
}