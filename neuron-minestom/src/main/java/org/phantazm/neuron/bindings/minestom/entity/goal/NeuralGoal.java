package org.phantazm.neuron.bindings.minestom.entity.goal;

import org.phantazm.commons.Activable;
import org.phantazm.neuron.bindings.minestom.entity.NeuralEntity;

/**
 * Represents a goal for a {@link NeuralEntity} that usually controls its behavior.
 */
public interface NeuralGoal extends Activable {

    /**
     * Gets whether the goal should start.
     *
     * @return Whether the goal should start
     */
    boolean shouldStart();

    /**
     * Gets whether the goal should end.
     *
     * @return Whether the goal should end
     */
    boolean shouldEnd();

}