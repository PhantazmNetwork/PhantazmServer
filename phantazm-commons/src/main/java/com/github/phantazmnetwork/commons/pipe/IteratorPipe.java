package com.github.phantazmnetwork.commons.pipe;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Pipe implementation based off of an iterator. Not part of the public API.
 * @param <TValue> the upper bound of the value held in the backing iterator
 */
final class IteratorPipe<TValue> implements Pipe<TValue> {
    private Iterator<? extends TValue> iterator;
    private boolean ended;

    /**
     * Creates a new pipe based off of the given iterator. Instances can be obtained through
     * {@link Pipe#from(Iterator)}.
     * @param iterator the backing iterator
     */
    IteratorPipe(@NotNull Iterator<? extends TValue> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        if(ended) {
            return false;
        }

        boolean hasNext = iterator.hasNext();
        if(!hasNext) {
            iterator = null;
            ended = true;
        }

        return hasNext;
    }

    @Override
    public TValue next() {
        if(ended) {
            throw new NoSuchElementException();
        }

        return iterator.next();
    }
}
