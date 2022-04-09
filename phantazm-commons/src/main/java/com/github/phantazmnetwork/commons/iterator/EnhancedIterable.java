package com.github.phantazmnetwork.commons.iterator;

import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link Iterable} that returns an {@link EnhancedIterator}.
 * @param <TValue> the value returned by the iterator
 */
@FunctionalInterface
public interface EnhancedIterable<TValue> extends Iterable<TValue> {
    @Override
    @NotNull EnhancedIterator<TValue> iterator();
}
