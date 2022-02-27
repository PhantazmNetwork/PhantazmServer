package com.github.phantazmnetwork.commons.collection.list;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * <p>An unsafe implementation of {@link GapList}. All unsafe methods will typically only throw an
 * {@link IndexOutOfBoundsException} if the given index is negative, or exceeds the bounds of the backing array itself,
 * not just the size of the list.</p>
 *
 * <p>This class should generally only be used when it is rigidly encapsulated, to prevent accidental or intentional
 * corruption of state through the use of unsafe methods.</p>
 * @param <TValue> the type of value stored in the list
 */
public class UnsafeGapList<TValue> extends GapListAbstract<TValue> {
    /**
     * {@inheritDoc}
     */
    public UnsafeGapList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    public UnsafeGapList(@NotNull Collection<? extends TValue> collection) {
        super(collection);
    }

    /**
     * {@inheritDoc}
     */
    public UnsafeGapList() {
        super();
    }

    @Override
    public void addUnsafe(int index, TValue element) {
        addInternal(index, element);
    }

    @Override
    public void setUnsafe(int index, TValue element) {
        array[computePhysical(index, arrayStart, gapStart, gapLength)] = element;
    }

    @Override
    public TValue getUnsafe(int index) {
        //noinspection unchecked
        return (TValue) array[computePhysical(index, arrayStart, gapStart, gapLength)];
    }

    @Override
    public void removeUnsafe(int index) {
        int target = removeIndex(index);
        array[target] = null;
        size--;
    }

    @Override
    public boolean isUnsafe() {
        return true;
    }
}
