package org.phantazm.core;

import com.github.steanky.toolkit.collection.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RangeUtils {
    private RangeUtils() {
    }

    /**
     * Returns an {@link Iterable} over a range of numbers.
     *
     * @param startInclusive the starting index (inclusive)
     * @param endExclusive   the ending index (exclusive)
     * @return an iterable over the range {@code [startInclusive, endExclusive)}; if
     * {@code startInclusive == endExclusive} the iterable will be empty
     */
    public static @NotNull Iterable<Integer> intIterable(int startInclusive, int endExclusive) {
        if (startInclusive > endExclusive) throw new IllegalArgumentException("startInclusive > endExclusive");
        if (startInclusive == endExclusive) return Iterators::iterator;

        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<>() {
                    private int cur = startInclusive;

                    @Override
                    public boolean hasNext() {
                        return cur < endExclusive;
                    }

                    @Override
                    public Integer next() {
                        int cur;
                        if ((cur = this.cur++) >= endExclusive) throw new NoSuchElementException();
                        return cur;
                    }
                };
            }
        };
    }
}
