package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface SolidIterable extends Iterable<Solid> {
    @NotNull SolidIterator solidIterator();

    @Override
    default @NotNull Iterator<Solid> iterator() {
        return solidIterator();
    }
}
