package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SingletonIterator<T> implements Iterator<T> {
    private boolean end;
    private T value;

    public SingletonIterator(@Nullable T value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !end;
    }

    @Override
    public T next() {
        if(end) {
            throw new NoSuchElementException();
        }

        end = true;

        T valueCopy = value;
        value = null;

        return valueCopy;
    }
}
