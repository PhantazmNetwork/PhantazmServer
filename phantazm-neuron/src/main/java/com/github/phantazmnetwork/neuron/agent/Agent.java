package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.engine.Destination;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Agent extends Vec3I {
    @NotNull Walker getWalker();

    @NotNull HeuristicCalculator getHeuristicCalculator();

    boolean reachedDestination(@NotNull Vec3I position, @NotNull Destination destination);
}