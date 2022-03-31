package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * An extension for {@link Iterable} providing a specialized implementation of {@link Iterator}.
 */
@FunctionalInterface
public interface SolidIterable extends Iterable<Solid> {
    /**
     * Obtains a new {@link SolidIterable} used to iterate over the elements provided by this iterable.
     * @return a new SolidIterator implementation
     */
    @NotNull SolidIterator solidIterator();

    @Override
    default @NotNull Iterator<Solid> iterator() {
        return solidIterator();
    }
}
