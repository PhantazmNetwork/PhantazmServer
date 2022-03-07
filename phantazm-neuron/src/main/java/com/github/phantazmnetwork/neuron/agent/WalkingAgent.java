package com.github.phantazmnetwork.neuron.agent;

/**
 * Represents a physical agent which walks along the ground (is subject to gravitation).
 * @see Agent
 * @see PhysicalAgent
 */
public interface WalkingAgent extends PhysicalAgent {
    /**
     * The jump height, as a floating-point value. This may be used by specialized {@link Explorer} instances to determine
     * whether certain upwards movements are valid. Generally, this is the number of units beyond which agents will not
     * be able to make an upwards jump.
     * @return the jump height
     */
    float getJumpHeight();

    /**
     * The fall tolerance, as a floating-point value. This may be used by specialized {@link Explorer} instance to
     * determine whether certain downwards movements are valid. Generally, this is interpreted as a simple threshold
     * beyond which agents will avoid certain fall distances.
     * @return the fall tolerance
     */
    float getFallTolerance();
}
