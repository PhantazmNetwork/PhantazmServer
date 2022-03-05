package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.node.Destination;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public interface Pathfinder {
    @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Destination destination);
}
