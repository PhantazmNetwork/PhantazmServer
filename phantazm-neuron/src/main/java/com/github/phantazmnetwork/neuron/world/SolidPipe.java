package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link Pipe} specializing in the ordered iteration of {@link Solid} objects.
 */
public interface SolidPipe extends Pipe<Solid> {
    /**
     * Sets the current pointer. This is the location that will be iterated by a subsequent call to {@code next}.
     * @param first the first index
     * @param second the second index
     * @param third the third index
     */
    void setPointer(int first, int second, int third);

    /**
     * Returns the order followed by this SolidPipe.
     * @return the order followed by this SolidPipe
     */
    @NotNull Space.Order getOrder();

    /**
     * Returns an object which describes iteration behavior (such as starting point, ending point, and direction for
     * all axes).
     * @return an object describing iteration behavior
     */
    @NotNull Space.Order.IterationVariables getVariables();

    /**
     * Returns the first index (the index which is iterated first).
     * @return the first index
     */
    int getFirst();

    /**
     * Returns the second index (the index which is iterated second).
     * @return the second index
     */
    int getSecond();

    /**
     * Returns the third index (the index which is iterated last).
     * @return the third index
     */
    int getThird();
}
