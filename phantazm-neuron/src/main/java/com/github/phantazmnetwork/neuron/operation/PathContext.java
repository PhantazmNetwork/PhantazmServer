package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.Destination;
import com.github.phantazmnetwork.neuron.engine.Pathfinder;
import org.jetbrains.annotations.NotNull;

public interface PathContext {
    @NotNull Agent getAgent();

    @NotNull Pathfinder getPathfinder();

    @NotNull Destination getDestination();
}
