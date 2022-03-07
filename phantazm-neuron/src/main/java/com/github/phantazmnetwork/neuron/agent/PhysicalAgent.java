package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.world.Collider;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Agent} which exists in physical space (and therefore has collision).
 * @see Agent
 * @see WalkingAgent
 */
public interface PhysicalAgent extends Agent {
    /**
     * Retrieves the {@link Collider} instance used to perform collision checks for this agent. This is often used for
     * navigation, to determine if the agent may walk to certain areas without running into something.
     * @return a Collider instance used to perform collision checking
     */
    @NotNull Collider getCollider();
}
