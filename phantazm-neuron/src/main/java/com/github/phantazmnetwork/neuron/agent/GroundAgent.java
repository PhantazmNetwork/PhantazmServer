package com.github.phantazmnetwork.neuron.agent;

/**
 * Represents a physical agent which walks along the ground.
 * @see Agent
 * @see PhysicalAgent
 */
public interface GroundAgent extends PhysicalAgent {
    float getJumpHeight();

    float getFallTolerance();
}
