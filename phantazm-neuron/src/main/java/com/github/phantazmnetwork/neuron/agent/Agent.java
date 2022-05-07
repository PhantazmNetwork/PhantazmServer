package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathCache;
import com.github.phantazmnetwork.neuron.navigator.Controller;
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
    boolean hasStartPosition();

    @NotNull Vec3I getStartPosition();

    /**
     * Returns the {@link Descriptor} used by this agent. Many distinct agents may share the same descriptor.
     * @return an AgentDescriptor describing this agent
     */
    @NotNull Descriptor getDescriptor();

    @NotNull Explorer getExplorer();

    @NotNull Controller getController();
}