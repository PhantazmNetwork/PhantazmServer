package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.Nullable;

public interface SolidSource {
    @Nullable Solid get(int x, int y, int z);
}
