package com.github.phantazmnetwork.commons.vector;

import java.util.function.Predicate;

/**
 * A {@link Predicate} operating on 3 integer values.
 */
public interface Vec3IPredicate {
    /**
     * Tests this predicate with the provided 3D vector.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return true if this test passes, false otherwise
     */
    boolean test(int x, int y, int z);
}
