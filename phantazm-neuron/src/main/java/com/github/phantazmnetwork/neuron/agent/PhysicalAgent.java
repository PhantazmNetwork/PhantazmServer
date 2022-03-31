package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.node.NodeTranslator;

/**
 * Represents an {@link Agent} which exists in physical space (and therefore has collision).
 * @see Agent
 * @see NodeTranslator
 */
public interface PhysicalAgent extends Agent {
    float getWidth();

    float getDepth();

    float getHeight();
}
