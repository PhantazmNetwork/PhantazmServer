package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple {@link Iterator} over a single value. This iterator does not support removal.
 * @param <TTYpe> the type of value
 */
public final class SingletonIterator<TTYpe> implements Iterator<TTYpe> {
    private boolean end;
    private TTYpe value;

    /**
     * Create a new SingletonIterator over the provided value.
     * @param value the single element to iterate
     */
    public SingletonIterator(@Nullable TTYpe value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !end;
    }

    @Override
    public TTYpe next() {
        if(end) {
            throw new NoSuchElementException();
        }

        end = true;

        TTYpe valueCopy = value;
        value = null;

        return valueCopy;
    }
}
