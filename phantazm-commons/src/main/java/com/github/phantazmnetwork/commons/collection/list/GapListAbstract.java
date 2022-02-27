package com.github.phantazmnetwork.commons.collection.list;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class containing functionality common to both {@link UnsafeGapList} and {@link SafeGapList}.
 * @param <TValue> the type of value contained in the list
 */
abstract class GapListAbstract<TValue> extends AbstractList<TValue> implements GapList<TValue>, RandomAccess {
    private static final int DEFAULT_INITIAL_CAPACITY = 8;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    protected Object[] array;

    protected int size;

    protected int arrayStart;
    protected int gapStart;
    protected int gapLength;

    /**
     * Creates a new {@link GapList} with the specified initial capacity (gap size).
     * @param initialCapacity the initial gap size
     */
    public GapListAbstract(int initialCapacity) {
        this.array = initialCapacity == 0 ? EMPTY_OBJECT_ARRAY : new Object[checkCapacity(initialCapacity)];
        this.size = 0;
        this.arrayStart = 0;
        this.gapStart = 0;
        this.gapLength = initialCapacity;
    }

    /**
     * Creates a new {@link GapList} containing the same elements as the given collection.
     * @param collection the collection whose elements will be used
     */
    public GapListAbstract(@NotNull Collection<? extends TValue> collection) {
        this.array = collection.toArray();
        this.size = array.length;
        this.arrayStart = 0;
        this.gapStart = array.length;
        this.gapLength = 0;
    }

    /**
     * Constructs a new {@link GapList} with the default initial capacity (gap size).
     */
    public GapListAbstract() {
        this.array = new Object[DEFAULT_INITIAL_CAPACITY];
        this.size = 0;
        this.arrayStart = 0;
        this.gapStart = 0;
        this.gapLength = DEFAULT_INITIAL_CAPACITY;
    }

    private static int checkCapacity(int capacity) {
        if(capacity < 0) {
            throw new IllegalArgumentException("Negative capacity");
        }

        return capacity;
    }

    /**
     * Utility method used to compute the physical index (which accounts for the gap) from a logical one (which does
     * not).
     * @param logical the logical (input) index
     * @param arrayStart the start of the array; is either 0 (if gapStart > 0) or gapLength (when gapStart == 0)
     * @param gapStart the starting index of the gap
     * @param gapLength the length of the gap
     * @return the physical (output) index
     */
    protected static int computePhysical(int logical, int arrayStart, int gapStart, int gapLength) {
        if(gapStart == 0) {
            return logical + gapLength;
        }

        int physical = arrayStart + logical;
        if(physical >= gapStart) {
            return physical + gapLength;
        }

        return physical;
    }

    /**
     * Performs a bounds check on the index, given the size, for an add operation.
     * @param index the index to check
     * @param size the size to check against
     * @throws IndexOutOfBoundsException if index is outside the range [0, size]
     */
    protected static void checkIndexAdd(int index, int size) {
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Out of bounds for add: index " + index + ", size " + size);
        }
    }

    /**
     * Performs a bounds check on the index, given the size.
     * @param index the index to check
     * @param size the size to check against
     * @throws IndexOutOfBoundsException if index is outside the range [0, size)
     */
    protected static void checkIndex(int index, int size) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Out of bounds: index " + index + ", size " + size);
        }
    }

    /**
     * Utility method similar to {@link Arrays#fill(Object[], int, int, Object)}, but does not perform a check on the
     * starting and ending indices, and only fills with null.
     * @param array the array to fill
     * @param start the starting index of the range (inclusive)
     * @param end the ending index of the range (exclusive)
     */
    protected static void fillWithNull(Object[] array, int start, int end) {
        for(int i = start; i < end; i++) {
            array[i] = null;
        }
    }

    /**
     * Grows the gap, given a certain minimum value. The actual size of the gap will be equal to twice the array length
     * if the array is small (less than 64 elements) plus the minimum value; otherwise, the new gap size will be half
     * the current length plus the minimum value.
     * @param by the amount to grow the gap by; must be > 0
     */
    protected void growGap(int by) {
        //double array when small, grow by 50% otherwise
        int newGapLength = (array.length < 64 ? array.length : (array.length >> 1)) + by;

        Object[] newArray = new Object[array.length + newGapLength];

        int oldGapEnd = gapStart + gapLength;
        System.arraycopy(array, 0, newArray, 0, gapStart);
        System.arraycopy(array, oldGapEnd, newArray, gapStart + newGapLength, array.length - oldGapEnd);

        gapLength = newGapLength;

        if(gapStart == 0 && array.length > 0) {
            arrayStart = gapStart + gapLength;
        }

        array = newArray;
    }

    /**
     * Shifts the gap by a certain number of indices. Negative numbers shift the gap closer to index 0, positive numbers
     * further from index 0. This function also ensures that all elements in range [gapStart, gapStart + gapLength) are
     * null after the shift occurs.
     * @param by the amount to shift the gap, negative moving to the left, positive moving to the right
     * @param afterGap gapStart + gapEnd, precomputed
     * @return the index directly after the (moved) gap
     */
    protected int moveGap(int by, int afterGap) {
        if(by < 0) {
            //gap being moved down
            int start = gapStart + by;
            int length = -by;
            int dest = start + gapLength;

            System.arraycopy(array, start, array, dest, length);
            fillWithNull(array, start, Math.min(start + length, dest));
        }
        else {
            //gap being moved up
            System.arraycopy(array, afterGap, array, gapStart, by);
            fillWithNull(array, Math.max(afterGap, gapStart + by), afterGap + by);
        }

        gapStart += by;
        afterGap += by;

        if(gapStart == 0) {
            arrayStart = afterGap;
        }
        else {
            arrayStart = 0;
        }

        return afterGap;
    }

    /**
     * Ensures that the gap is sized and positioned appropriately to contain the given number of expected elements. The
     * gap will only be moved/expanded if it is necessary to do so.
     * @param logicalIndex the logical index of the element which will be positioned directly before the gap after this
     *                     method returns
     * @param elements the number of elements we need the gap to contain
     * @return the starting location to which elements should be inserted into the gap
     */
    protected int ensureGap(int logicalIndex, int elements) {
        if(elements > gapLength) {
            //gap is too small to contain everything, scale it up
            growGap(elements);
        }

        if(logicalIndex == gapStart) {
            gapLength -= elements;

            if(gapStart == size) {
                int oldGapStart = gapStart;
                gapStart += elements;
                return oldGapStart;
            }

            int newEnd = gapStart + gapLength;

            if(gapStart == 0) {
                arrayStart = newEnd;
            }

            return newEnd;
        }

        int gapEnd = moveGap(logicalIndex - gapStart, gapStart + gapLength);
        gapLength -= elements;

        return gapEnd - elements;
    }

    /**
     * Handles the logic of element removal, may move the gap if necessary.
     * @param logicalIndex the index which is to be removed
     * @return the index of the element that should be set to null
     */
    protected int removeIndex(int logicalIndex) {
        modCount++;

        if(logicalIndex == gapStart) {
            gapLength++;

            if(gapStart == 0) {
                arrayStart++;
            }

            return logicalIndex;
        }

        int beforeStart = gapStart - 1;
        if(logicalIndex == beforeStart) {
            //handle the simple case where we shift the gap back and increase its size
            gapStart = beforeStart;
            gapLength++;

            if(gapStart == 0) {
                arrayStart = gapStart + gapLength;
            }

            return gapStart;
        }

        int gapEnd = moveGap(logicalIndex - gapStart, gapStart + gapLength);
        gapLength++;
        return gapEnd;
    }

    /**
     * Common logic for adding elements.
     * @param index the index to add at
     * @param element the element to add
     */
    protected void addInternal(int index, TValue element) {
        modCount++;
        int target = ensureGap(index, 1);
        array[target] = element;
        size++;
    }

    /* public methods */

    @Override
    public TValue set(int index, TValue element) {
        checkIndex(index, size);
        int physicalIndex = computePhysical(index, arrayStart, gapStart, gapLength);

        Object oldElement = array[physicalIndex];
        array[physicalIndex] = element;

        //noinspection unchecked
        return (TValue) oldElement;
    }

    @Override
    public boolean add(TValue value) {
        addInternal(size, value);
        return true;
    }

    @Override
    public void add(int index, TValue element) {
        checkIndexAdd(index, size);
        addInternal(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TValue> collection) {
        checkIndexAdd(index, size);
        modCount++;

        Object[] collectionArray = collection.toArray();
        if(collectionArray.length == 0) {
            return false;
        }

        int gapEnd = ensureGap(index, collectionArray.length);
        System.arraycopy(collectionArray, 0, array, gapEnd, collectionArray.length);
        size += collectionArray.length;

        return true;
    }

    @Override
    public TValue remove(int index) {
        checkIndex(index, size);
        int target = removeIndex(index);

        Object oldValue = array[target];
        array[target] = null;

        size--;

        //noinspection unchecked
        return (TValue) oldValue;
    }

    @Override
    public TValue get(int index) {
        checkIndex(index, size);

        //noinspection unchecked
        return (TValue) array[computePhysical(index, arrayStart, gapStart, gapLength)];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        modCount++;
        fillWithNull(array, 0, array.length);

        size = 0;
        arrayStart = 0;
        gapStart = 0;
        gapLength = array.length;
    }

    /* "only" methods */

    @Override
    public void setOnly(int index, TValue element) {
        checkIndex(index, size);
        array[computePhysical(index, arrayStart, gapStart, gapLength)] = element;
    }

    @Override
    public void removeOnly(int index) {
        checkIndex(index, size);
        int target = removeIndex(index);
        array[target] = null;
        size--;
    }
}