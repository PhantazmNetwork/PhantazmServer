package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class IteratorUtils {
    private IteratorUtils() {throw new UnsupportedOperationException(); }

    private static final Iterator<?> EMPTY = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    public static <T> Iterator<T> empty() {
        //noinspection unchecked
        return (Iterator<T>) EMPTY;
    }

    public static <T> Iterator<T> unmodifiable(@NotNull Iterator<? extends T> iterator) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }
}
