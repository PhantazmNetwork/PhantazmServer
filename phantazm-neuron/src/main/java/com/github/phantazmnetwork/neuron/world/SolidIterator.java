package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface SolidIterator extends Iterator<Solid> {
    void setPointer(int first, int second, int third);

    @NotNull Space.Order.IterationVariables getVariables();

    int getFirst();

    int getSecond();

    int getThird();
}
