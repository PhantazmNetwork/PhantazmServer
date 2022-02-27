package com.github.phantazmnetwork.commons.collection.list;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * <p>A safe implementation of {@link GapList}. All unsafe methods will throw an
 * {@link UnsupportedOperationException}.</p>
 *
 * <p>For lists whose access is not strictly controlled to a small set of classes (for example, a list which is
 * accessible through a public API), this implementation of {@link GapList} should be used as it prevents accidental or
 * malicious modification of the list's state through unsafe methods.</p>
 * @param <TValue> the type of value stored in the list
 */
public class SafeGapList<TValue> extends GapListAbstract<TValue> {
    /**
     * @see GapListAbstract#GapListAbstract(int)
     */
    public SafeGapList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @see GapListAbstract#GapListAbstract(Collection)
     */
    public SafeGapList(@NotNull Collection<? extends TValue> collection) {
        super(collection);
    }

    /**
     * @see GapListAbstract#GapListAbstract()
     */
    public SafeGapList() {
        super();
    }

    @Override
    public void addUnsafe(int index, TValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TValue getUnsafe(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUnsafe(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUnsafe(int index, TValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUnsafe() {
        return false;
    }
}
