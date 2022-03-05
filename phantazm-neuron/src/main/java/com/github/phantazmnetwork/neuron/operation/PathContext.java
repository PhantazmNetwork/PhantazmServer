package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.Pathfinder;
import com.github.phantazmnetwork.neuron.node.Destination;
import org.jetbrains.annotations.NotNull;

public interface PathContext {
    @NotNull Pathfinder getPathfinder();

    @NotNull Agent getAgent();

    @NotNull Destination getDestination();
}
