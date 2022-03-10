package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.Nullable;

public interface Space {
    @Nullable Solid solidAt(int x, int y, int z);
}
