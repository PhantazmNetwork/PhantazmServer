package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something capable of pathfinding. This is the most general representation of a navigation-capable object,
 * and generally all that is required to be used by a pathfinding algorithm such as A*.
 */
public interface Agent {
    /**
     * Determines if the agent has a starting location. In other words, returns {@code true} if the agent is capable of
     * pathfinding, and {@code false} if it isn't.
     * @return {@code true} if this agent has a starting position (is valid for pathing); {@code false} otherwise
     */
    boolean canPathfind();

    /**
     * Returns the starting position for this Agent.
     * @return the starting position for this Agent
     */
    @NotNull Vec3I getStartPosition();

    /**
     * Returns the {@link Descriptor} used by this agent. Many distinct agents may share the same descriptor.
     * @return an AgentDescriptor describing this agent
     */
    @NotNull Descriptor getDescriptor();

    /**
     * Returns the {@link Explorer} used by this agent to expand {@link Node}s when pathfinding.
     * @return the Explorer instance used to expand nodes
     */
    @NotNull Explorer getExplorer();

    /**
     * Returns the {@link Controller} used to physically move this Agent along a completed path.
     * @return the controller used to position this agent
     */
    @NotNull Controller getController();
}