package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

public interface PhysicalContext extends PathContext {
    @NotNull Space getSpace();
}
