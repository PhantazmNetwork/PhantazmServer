package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Contains some utility methods for {@link Iterator}s. This class cannot be instantiated.
 */
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

    /**
     * Returns a shared "empty" {@link Iterator} whose {@code hasNext} method always returns false, and whose
     * {@code next} method always throws {@link NoSuchElementException}.
     * @param <T> the type of object iterated by the empty iterator
     * @return the shared empty iterator, cast to the required type
     */
    public static <T> @NotNull Iterator<T> empty() {
        //noinspection unchecked
        return (Iterator<T>) EMPTY;
    }

    /**
     * Creates a new {@link Iterator} instance wrapping the provided one. The new Iterator does not implement the
     * optional method {@link Iterator#remove()}.
     * @param iterator the iterator to wrap
     * @param <T> the type of object iterated by the new iterator
     * @return a new, unmodifiable iterator (one that does not support element removal)
     * @throws NullPointerException if iterator is null
     */
    public static <T> @NotNull Iterator<T> unmodifiable(@NotNull Iterator<? extends T> iterator) {
        Objects.requireNonNull(iterator, "iterator");

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

    /**
     * Iterates both iterators, checking each returned element. If both iterators return the same number of elements,
     * and every element is equal, and in the same order, this method returns true. Otherwise, it returns false.
     * @param first the first iterator to check
     * @param second the second iterator to check
     * @return true if the iterators are equal, false otherwise
     * @param <T> a common supertype for both iterators
     */
    public static <T> boolean iteratorEquals(@NotNull Iterator<? extends T> first,
                                             @NotNull Iterator<? extends T> second) {
        while (true) {
            boolean firstHasNext = first.hasNext();
            boolean secondHasNext = second.hasNext();

            if(!firstHasNext && !secondHasNext) {
                return true;
            }

            if(firstHasNext != secondHasNext) {
                return false;
            }

            if(!Objects.equals(first.next(), second.next())) {
                return false;
            }
        }
    }
}
