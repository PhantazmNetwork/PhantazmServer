package com.github.phantazmnetwork.commons.pipe;

import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * A {@link Pipe} object containing a single instance. Not part of the public API.
 * @param <TTYpe> the type of element contained in this pipe
 */
final class SingletonPipe<TTYpe> implements Pipe<TTYpe> {
    private boolean end;
    private TTYpe value;

    /**
     * Creates a new SingletonPipe containing the specified value. Instances may be obtained through
     * {@link Pipe#of(Object[])}.
     * @param value the only value contained in this pipe
     */
    SingletonPipe(@Nullable TTYpe value) {
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
