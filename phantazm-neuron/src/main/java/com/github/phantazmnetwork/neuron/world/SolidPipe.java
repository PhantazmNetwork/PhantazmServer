package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface SolidPipe extends Pipe<Solid> {
    void setPointer(int first, int second, int third);

    @NotNull Space.Order getOrder();

    @NotNull Space.Order.IterationVariables getVariables();

    int getFirst();

    int getSecond();

    int getThird();
}
