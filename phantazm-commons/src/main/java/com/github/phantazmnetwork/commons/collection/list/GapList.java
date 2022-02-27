package com.github.phantazmnetwork.commons.collection.list;

import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;

/**
 * <p>A specialized array-backed {@link List} whose structurally modifying operations (i.e. {@link GapList#add(Object)},
 * {@link GapList#remove(int)}, ...) are optimized for situations in which there is a strong locality of index, in which
 * most structural modifications occur at indices that are near to each other.</p>
 *
 * <p>Generally speaking, implementations should be much more performant than {@link ArrayList} for many consecutive
 * additions or removals to or at at the same index, so long as that index is not at the end of the list (where
 * performance is roughly on par with ArrayList). Many inserts to the beginning are about as fast as
 * {@link ArrayDeque}</p>
 *
 * <p>Some notable methods included in this interface are {@link GapList#setOnly(int, Object)} and
 * {@link GapList#removeOnly(int)}. These behave identically to their counterparts {@link GapList#set(int, Object)} and
 * {@link GapList#remove(int)} other than in that they do not perform an additional array access to retrieve and return
 * the value previously stored at the given index.</p>
 *
 * <p>This interface also specifies a number of so-called "unsafe" methods: {@link GapList#addUnsafe(int, Object)},
 * {@link GapList#setUnsafe(int, Object)}, {@link GapList#removeUnsafe(int)} and {@link GapList#getUnsafe(int)}. These
 * are equivalent to {@link GapList#add(Object)}, {@link GapList#set(int, Object)}, and {@link GapList#get(int)},
 * respectively, but they may eliminate certain run-time checks that are designed to prevent state corruption/indicate
 * error, typically related to <i>bounds-checking</i>. This can provide measurable performance increases in cases where
 * the programmer is sure that the index <b>must</b> be valid, perhaps because they have already performed such checks
 * themselves, or it can otherwise be statically proven no out-of-bounds condition is possible.</p>
 *
 * <p>It must be noted that use of unsafe methods is inherently dangerous and can cause irreparable state corruption of
 * the list, the "broken" nature of which may only be apparent later. The behavior of unsafe methods when passed an
 * out-of-bounds index is undefined.</p>
 *
 * <p>The unsafe methods are optional. Implementations that do not want to support them must throw a
 * {@link UnsupportedOperationException}, and have their {@link GapList#isUnsafe()} method return false. Implementations
 * that want to support these methods must support all of them, and must have isUnsafe return true.</p>
 * @param <TValue> the type of object held in this list
 * @see SafeGapList
 * @see UnsafeGapList
 */
public interface GapList<TValue> extends List<TValue> {
    /**
     * Similar to {@link GapList#set(int, Object)}, but does not retrieve the value previously located at the given
     * index.
     * @param index the index of the value to replace
     * @param value the new value, which will replace the value previously located at index
     */
    void setOnly(int index, TValue value);

    /**
     * Similar to {@link GapList#remove(int)}, but does not retrieve the value previously located at the given index.
     * @param index the index of the value to remove
     */
    void removeOnly(int index);

    /**
     * <p><b>Unsafe method</b>. Similar to {@link GapList#add(int, Object)}, but does not perform a range check on the
     * index.</p>
     *
     * <p>It is rarely appropriate to use this method in preference to its normal counterpart. See the description of
     * this interface for a more detailed explanation of unsafe methods.</p>
     * @param index the index where the element will be added, which will not be range-checked
     * @param value the value to add
     */
    void addUnsafe(int index, TValue value);

    /**
     * <p><b>Unsafe method</b>. Similar to {@link GapList#set(int, Object)}, but does not perform a range check on the
     * index.</p>
     *
     * <p>It is rarely appropriate to use this method in preference to its normal counterpart. See the description of
     * this interface for a more detailed explanation of unsafe methods.</p>
     * @param index the index of the element to replace, which will not be range-checked
     * @param value the new value
     */
    void setUnsafe(int index, TValue value);

    /**
     * <p><b>Unsafe method</b>. Similar to {@link GapList#get(int)}, but does not perform a range check on the
     * index.</p>
     *
     * <p>It is rarely appropriate to use this method in preference to its normal counterpart. See the description of
     * this interface for a more detailed explanation of unsafe methods.</p>
     * @param index the index of the element to get, which will not be range-checked
     */
    TValue getUnsafe(int index);

    /**
     * <p><b>Unsafe method</b>. Similar to {@link GapList#remove(int)}, but does not perform a range check on the
     * index.</p>
     *
     * <p>It is rarely appropriate to use this method in preference to its normal counterpart. See the description of
     * this interface for a more detailed explanation of unsafe methods.</p>
     * @param index the index of the element to remove, which will not be range-checked
     */
    void removeUnsafe(int index);

    /**
     * Can be used to determine if this GapList implementation is unsafe. If it is — or in other words, if its unsafe
     * methods can be used without throwing an {@link UnsupportedOperationException} — this method will return true.
     * In all other cases it will return false.
     * @return true if this implementation is unsafe, false otherwise
     */
    boolean isUnsafe();
}
