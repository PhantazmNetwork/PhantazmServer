package com.github.phantazmnetwork.commons.pipe;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 * A {@link Pipe} implementation with a backing array. Not part of the public API.
 *
 * @param <TComponent> the component type of the backing array
 */
final class ArrayPipe<TComponent> implements Pipe<TComponent> {
    private TComponent[] array;
    private int index;

    /**
     * Creates a new ArrayPipe from the given array. Instances can be obtained through calls to
     * {@link Pipe#of(Object[])}.
     *
     * @param array the backing array
     */
    ArrayPipe(TComponent @NotNull [] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return array != null && index < array.length;
    }

    @Override
    public TComponent next() {
        if (!hasNext()) {
            array = null;
            throw new NoSuchElementException();
        }

        TComponent element = array[index++];
        if (index == array.length) {
            array = null;
        }

        return element;
    }
}
