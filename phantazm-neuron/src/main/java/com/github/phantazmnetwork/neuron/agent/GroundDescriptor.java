package com.github.phantazmnetwork.neuron.agent;

/**
 * A {@link Descriptor} for agents that are subject to gravity and can jump or fall.
 */
public interface GroundDescriptor extends PhysicalDescriptor {
    /**
     * Returns the jump height for this agent.
     * @return the jump height for this agent
     */
    float getJumpHeight();

    /**
     * Returns the fall tolerance for this agent. This is the maximum value beyond which agents will no longer be able
     * to pathfind down a vertical drop.
     * @return the fall tolerance for this agent
     */
    float getFallTolerance();
}
