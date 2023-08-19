package org.phantazm.commons;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;

/**
 * Utility class for common, shared hashing strategies. This class cannot be instantiated.
 *
 * @see Hash.Strategy
 */
public final class HashStrategies {
    private static final Hash.Strategy<?> IDENTITY = new Hash.Strategy<>() {
        @Override
        public int hashCode(Object o) {
            return System.identityHashCode(o);
        }

        @Override
        public boolean equals(Object a, Object b) {
            return a == b;
        }
    };

    private HashStrategies() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an identity-based hashing strategy. Can be used to enable {@code fastutil} custom hashmaps to operate
     * similarly to {@link IdentityHashMap}.
     *
     * @param <T> the key type for the map in which this is to be used
     * @return an identity-based hash strategy
     */
    @SuppressWarnings("unchecked")
    public static <T> Hash.@NotNull Strategy<T> identity() {
        //this cast is always safe, IDENTITY's methods don't care about the object type
        return (Hash.Strategy<T>) IDENTITY;
    }
}
