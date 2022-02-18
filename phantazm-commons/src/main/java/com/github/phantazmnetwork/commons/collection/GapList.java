package com.github.phantazmnetwork.commons.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p>A specialized array-backed {@link List} implementation whose structurally modifying operations (i.e.
 * {@link GapList#add(Object)}, {@link GapList#remove(int)}, ...) are optimized for situations in which there is a
 * strong locality of index (a situation in which most structural modifications occur at indices that are near to each
 * other).</p>
 *
 * <p>Many consecutive inserts at the same index run <i>much</i> faster (on the order of 1000%) than {@link ArrayList}.
 * Consecutive appends run roughly as fast as ArrayList. Consecutive inserts at the beginning run somewhat (5-10%)
 * slower than {@link ArrayDeque}.</p>
 * @param <TValue> the type of object held in this list
 */
public class GapList<TValue> extends AbstractList<TValue> implements RandomAccess {
    private static final int DEFAULT_INITIAL_CAPACITY = 8;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private Object[] array;

    private int size;

    private int arrayStart;
    private int gapStart;
    private int gapLength;

    /**
     * Creates a new GapList with the specified initial capacity (gap size).
     * @param initialCapacity the initial gap size
     */
    public GapList(int initialCapacity) {
        this.array = initialCapacity == 0 ? EMPTY_OBJECT_ARRAY : new Object[checkCapacity(initialCapacity)];
        this.size = 0;
        this.arrayStart = 0;
        this.gapStart = 0;
        this.gapLength = initialCapacity;
    }

    /**
     * Creates a new GapList containing the same elements as the given collection.
     * @param collection the collection whose elements will be used
     */
    public GapList(@NotNull Collection<? extends TValue> collection) {
        this.array = collection.toArray();
        this.size = array.length;
        this.arrayStart = 0;
        this.gapStart = array.length;
        this.gapLength = 0;
    }

    /**
     * Constructs a new GapList with the default initial capacity (gap size).
     */
    public GapList() {
        this.array = new Object[DEFAULT_INITIAL_CAPACITY];
        this.size = 0;
        this.arrayStart = 0;
        this.gapStart = 0;
        this.gapLength = DEFAULT_INITIAL_CAPACITY;

        Arrays.fill(array, null);
    }

    private static int checkCapacity(int capacity) {
        if(capacity < 0) {
            throw new IllegalArgumentException("Negative capacity");
        }

        return capacity;
    }

    private static int computePhysical(int logical, int arrayStart, int gapStart, int gapLength) {
        if(gapStart == 0) {
            return logical + gapLength;
        }

        int physical = arrayStart + logical;
        if(physical >= gapStart) {
            return physical + gapLength;
        }

        return physical;
    }

    private static void checkIndexAdd(int index, int size) {
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Out of bounds for add: index " + index + ", size " + size);
        }
    }

    private static void checkIndex(int index, int size) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Out of bounds: index " + index + ", size " + size);
        }
    }

    private static void fillWithNull(Object[] array, int start, int end) {
        for(int i = start; i < end; i++) {
            array[i] = null;
        }
    }

    /*
    Grows the gap, given a certain minimum value. The actual size of the gap will be equal to twice the array length if
    the array is small (less than 64 elements) plus the minimum value; otherwise, the new gap size will be half the
    current length plus the minimum value.
     */
    private void growGap(int by) {
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

    /*
    Shifts the gap by a certain number of indices. Negative numbers shift the gap closer to index 0, positive numbers
    further from index 0. This function also ensures that all elements in range [gapStart, gapStart + gapLength) are
    null after the shift occurs.
     */
    private int moveGap(int by, int afterGap) {
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

    private int ensureGap(int logicalIndex, int elements) {
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

    //handles the logic of element removal, may move the gap if necessary
    //index returned is the index of the element that should be set to null
    private int removeIndex(int logicalIndex) {
        checkIndex(logicalIndex, size);
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

    private void addInternal(int index, TValue element) {
        modCount++;
        int target = ensureGap(index, 1);
        array[target] = element;
        size++;
    }

    @Override
    public TValue set(int index, TValue element) {
        checkIndex(index, size);
        int physicalIndex = computePhysical(index, arrayStart, gapStart, gapLength);

        Object oldElement = array[physicalIndex];
        array[physicalIndex] = element;

        //noinspection unchecked
        return (TValue) oldElement;
    }

    /**
     * Alternative to {@link GapList#set(int, Object)} that does not perform an additional array access to retrieve the
     * item previously located at the given index.
     * @param index the index to set at
     * @param element the element to set
     * @throws IndexOutOfBoundsException if index is outside the range [0, size)
     */
    public void setOnly(int index, TValue element) {
        checkIndex(index, size);
        array[computePhysical(index, arrayStart, gapStart, gapLength)] = element;
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
        int target = removeIndex(index);

        Object oldValue = array[target];
        array[target] = null;

        size--;

        //noinspection unchecked
        return (TValue) oldValue;
    }

    /**
     * Alternative to {@link GapList#remove(int)} that does not perform an additional array access to retrieve the item
     * previously located at the given index.
     * @param index the index to remove at
     * @throws IndexOutOfBoundsException if index is outside the range [0, size)
     */
    public void removeOnly(int index) {
        int target = removeIndex(index);
        array[target] = null;
        size--;
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
}