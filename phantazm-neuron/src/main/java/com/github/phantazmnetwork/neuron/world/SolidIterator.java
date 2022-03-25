package com.github.phantazmnetwork.neuron.world;

import java.util.Iterator;

public interface SolidIterator extends Iterator<Solid> {
    void setPointer(int first, int second, int third);
}
