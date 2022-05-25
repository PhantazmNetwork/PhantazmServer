package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.Nullable;

/**
 * <p>Represents a <i>constant pool</i> of {@link Vec3I} instances. This is conceptually similar to the constant pools
 * maintained by the JVM for strings and certain boxed primitives.</p>
 *
 * <p>Implementations <i>must</i> guarantee thread safety for {@link Vec3IPool#fromCache(int, int, int)}. Since cache
 * lookups generally occur very frequently, lock contention is a possibility and should be taken into consideration.</p>
 *
 * <p>This interface is only used internally and is not part of the public API.</p>
 */
@FunctionalInterface
interface Vec3IPool {
    /**
     * Returns a value from cache with the components (x, y, z). If no such component exists, {@code null} is returned.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return a cached vector, or null if it cannot be found
     */
    @Nullable Vec3I fromCache(int x, int y, int z);
}
