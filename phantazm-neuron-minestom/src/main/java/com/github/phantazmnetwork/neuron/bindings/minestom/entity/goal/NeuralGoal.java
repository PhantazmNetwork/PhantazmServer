package com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;

/**
 * Represents a goal for a {@link NeuralEntity} that usually controls its behavior.
 */
public interface NeuralGoal extends Tickable {

    /**
     * Gets whether the goal should start.
     * @return Whether the goal should start
     */
    boolean shouldStart();

    /**
     * Starts the goal.
     */
    void start();

    /**
     * Gets whether the goal should end.
     * @return Whether the goal should end
     */
    boolean shouldEnd();

    /**
     * Ends the goal.
     */
    void end();

}
