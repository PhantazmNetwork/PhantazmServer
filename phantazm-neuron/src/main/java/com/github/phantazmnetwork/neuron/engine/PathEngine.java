package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;
import com.github.phantazmnetwork.neuron.operation.PathOperation;

import java.util.concurrent.Future;

/**
 * The main entrypoint for Neuron. Responsible for creating, scheduling and executing {@link PathOperation} instances.
 * Implementations may run synchronously or asynchronously.
 */
@FunctionalInterface
public interface PathEngine {
    @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Vec3I destination);
}
