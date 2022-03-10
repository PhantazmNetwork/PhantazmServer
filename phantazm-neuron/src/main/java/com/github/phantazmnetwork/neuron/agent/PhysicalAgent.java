package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.world.NodeTranslator;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Agent} which exists in physical space (and therefore has collision).
 * @see Agent
 * @see NodeTranslator
 */
public interface PhysicalAgent extends Agent {
    /**
     * Retrieves the {@link NodeTranslator} instance which calculates the translation vector resulting from an attempt
     * to move this agent in a specific direction.
     * @return a Collider instance used to perform collision checking
     */
    @NotNull NodeTranslator getNodeTranslator();

    float getWidth();

    float getHeight();
}
