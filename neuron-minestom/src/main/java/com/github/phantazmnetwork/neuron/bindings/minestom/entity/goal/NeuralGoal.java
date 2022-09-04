package com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.steanky.element.core.annotation.Model;

/**
 * Represents a goal for a {@link NeuralEntity} that usually controls its behavior.
 */
@Model("neuron.goal.goal")
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
