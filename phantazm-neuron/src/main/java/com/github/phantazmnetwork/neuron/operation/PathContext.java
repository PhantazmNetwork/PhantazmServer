package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.Pathfinder;
import com.github.phantazmnetwork.neuron.node.Destination;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the current pathfinding context. Used to initialize {@link PathOperation} instances, providing them with
 * enough data to calculate their paths.
 */
public interface PathContext {
    /**
     * Returns the {@link Pathfinder} responsible for calculating the current path.
     * @return the Pathfinder calculating this path
     */
    @NotNull Pathfinder getPathfinder();

    /**
     * Returns the {@link Agent} that's attempting to pathfind.
     * @return the Agent currently pathfinding
     */
    @NotNull Agent getAgent();

    /**
     * Returns the {@link Destination} object representing the path's goal.
     * @return the target destination
     */
    @NotNull Destination getDestination();
}
