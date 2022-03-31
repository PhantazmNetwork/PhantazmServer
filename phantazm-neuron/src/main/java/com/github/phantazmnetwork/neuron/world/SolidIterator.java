package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * SolidIterator is a specialized extension of {@link Iterator} designed for iterating over solids in a defined
 * region of 3D space. The position of the iterator can be controlled using the {@code setPointer} method.
 */
public interface SolidIterator extends Iterator<Solid> {
    /**
     * <p>Sets the current position of this SolidIterator. Since the iteration order of SolidIterator is not strictly
     * bound to a specific arrangement of vector components (e.g. {@code xyz}, {@code yxz}, ...), {@code first},
     * {@code second}, and {@code third} may correspond to any permutation thereof. Which argument maps to which
     * coordinate can be determined by checking the value returned by {@link SolidIterator#getOrder()}.</p>
     *
     * <p>Note that if the pointer is set to a value where any component is "beyond" the defined endpoint (which may
     * occur when any component is greater than or less than the endpoint, depending on iteration direction), this
     * method will throw an {@link IllegalArgumentException}.</p>
     * @param first the first position to update
     * @param second the second position to update
     * @param third the third position to update
     * @throws IllegalArgumentException if the pointer is out of bounds
     */
    void setPointer(int first, int second, int third);

    @NotNull Space.Order getOrder();

    @NotNull Space.Order.IterationVariables getVariables();

    int getFirst();

    int getSecond();

    int getThird();
}
