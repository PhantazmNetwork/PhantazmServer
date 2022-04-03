package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * An extension for {@link Iterable} providing a specialized implementation of {@link Iterator}.
 */
@FunctionalInterface
public interface SolidIterable extends Iterable<Solid> {
    @Override
    @NotNull SolidIterator iterator();
}
