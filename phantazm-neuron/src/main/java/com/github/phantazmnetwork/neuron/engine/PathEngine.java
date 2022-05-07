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
    /**
     * Schedules a pathfinding operation. The returned {@link Future} may be used to query or await the
     * {@link PathResult} it produces. This may be null if the operation was cancelled due to an error or PathEngine
     * termination.
     * @param agent the agent to pathfind with
     * @param destination the agent's desired destination
     * @return a Future object representing the ongoing computation, whose result <i>may be null</i> if an error
     * occurred during pathfinding, or if the computation was cancelled due to PathEngine termination or other reasons
     */
    @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Vec3I destination);
}
