package com.github.phantazmnetwork.neuron.agent;

/**
 * A {@link Descriptor} implementation for agents that have width, depth, and height.
 */
public interface PhysicalDescriptor extends Descriptor {
    /**
     * Returns the width of this agent (x-length).
     * @return the width of this agent
     */
    float getWidth();

    /**
     * Returns the depth of this agent (z-length).
     * @return the depth of this agent
     */
    float getDepth();

    /**
     * Returns the height of this agent (y-length).
     * @return the height of this agent
     */
    float getHeight();
}
