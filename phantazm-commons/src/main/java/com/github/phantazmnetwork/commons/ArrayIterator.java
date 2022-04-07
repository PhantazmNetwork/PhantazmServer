package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A simple iterator over an array. This iterator does not support removal.
 * @param <TComponent> the component type of the array
 */
public final class ArrayIterator<TComponent> implements Iterator<TComponent> {
    private TComponent[] array;
    private int index;

    /**
     * Creates a new ArrayIterator over the provided array, which must not be null (but may contain null components).
     * @param array the array
     * @throws NullPointerException if array is null
     */
    public ArrayIterator(TComponent @NotNull [] array) {
        this.array = Objects.requireNonNull(array, "array");
    }

    @Override
    public boolean hasNext() {
        return array != null && index < array.length;
    }

    @Override
    public TComponent next() {
        if(index >= array.length) {
            throw new NoSuchElementException();
        }

        TComponent element = array[index++];
        if(index == array.length) {
            array = null;
        }
        return element;
    }
}
