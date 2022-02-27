package com.github.phantazmnetwork.commons.collection.map;

import com.github.phantazmnetwork.commons.collection.list.GapList;
import com.github.phantazmnetwork.commons.collection.list.UnsafeGapList;

/**
 * <p>Very slow, but should be extremely memory-efficient in comparison to more naive implementations like
 * {@link HashSpatialMap}. Still, this is pending removal, should probably not be used except as a reference for some
 * similar algorithms.</p>
 *
 * <p>Associates each point in the space of all 3D vectors with a (conceptual) 96-bit unsigned integer. This integer is
 * used to order each point, stored with its value (together called a "node"), in a sorted list. Retrieving an element
 * given the vector is as simple as constructing the 96-bit unsigned integer from said vector and conducting a binary
 * search on all of the elements.</p>
 *
 * <p>In general, because of the binary search required to both add and access elements,
 * {@link OffsetSpatialMap#get(int, int, int)} and {@link OffsetSpatialMap#put(int, int, int, Object)} have O(log(n))
 * time complexity.</p>
 * @param <T> the type of object stored in this map
 */
public class OffsetSpatialMap<T> implements SpatialMap<T> {
    private record Node(Object element, int high, long low) { }

    private final GapList<Node> elements;

    public OffsetSpatialMap() {
        this.elements = new UnsafeGapList<>();
    }

    //packs two ints into long, result will be negative only if y value is negative
    private static long indexLow(int y, int z) {
        return ((long) y << 32) | z;
    }

    //compares 2 96-bit unsigned integers
    private static int compareIndices(int highFirst, long lowFirst, int highSecond, long lowSecond) {
        int highComp = Integer.compareUnsigned(highFirst, highSecond);
        if(highComp == 0) {
            return Long.compareUnsigned(lowFirst, lowSecond);
        }

        return highComp;
    }

    //binary search through GapList, comparing 96-bit numbers
    private static int indexOf(int highIndex, long lowIndex, GapList<Node> elements){
        int low = 0;
        int high = elements.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Node midVal = elements.getUnsafe(mid);

            int compResult = compareIndices(midVal.high, midVal.low, highIndex, lowIndex);
            if (compResult < 0) {
                low = mid + 1;
            }
            else if (compResult > 0) {
                high = mid - 1;
            }
            else {
                return mid;
            }
        }

        return -(low + 1);
    }

    @Override
    public T get(int x, int y, int z) {
        int index = indexOf(x, indexLow(y, z), elements);
        if(index < 0) {
            return null;
        }

        //noinspection unchecked
        return (T)elements.getUnsafe(index).element;
    }

    @Override
    public void put(int x, int y, int z, T value) {
        long low = indexLow(y, z);
        int index = indexOf(x, low, elements);

        Node newNode = new Node(value, x, low);
        if(index < 0) {
            elements.addUnsafe((-index) - 1, newNode);
        }
        else {
            elements.setUnsafe(index, newNode);
        }
    }

    @Override
    public boolean containsKey(int x, int y, int z) {
        return indexOf(x, indexLow(y, z), elements) == 0;
    }
}
