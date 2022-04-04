package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Calculator;
import org.jetbrains.annotations.NotNull;

/**
 * Describes the characteristics (type) of an agent. Many agents may share a single descriptor. Descriptors encapsulate
 * the pathfinding behavior of agents — agents will identical descriptors will exhibit identical behavior.
 */
public interface Descriptor {
    /**
     * Returns the identification string used by this descriptor. Agents whose descriptors have equal ids may be able to
     * access the same cached values for certain operations.
     * @return the ID string for this descriptor
     */
    @NotNull String getID();

    /**
     * Returns the {@link Calculator} used to compute distance/heuristic values for this agent.
     * @return the Calculator instance which should be used by this agent
     */
    @NotNull Calculator getCalculator();

    boolean isComplete(@NotNull Vec3I position, @NotNull Vec3I destination);
}
