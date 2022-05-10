package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;
import com.github.phantazmnetwork.neuron.agent.Agent;

/**
 * <p>Represents an object which may be used to coerce an individual {@link Agent} along a path. Neuron does not provide
 * any concrete implementations of this interface, as it is intended to be designed in close cooperation with the
 * platform being used.</p>
 *
 * <p>Separate implementations generally must be created for each "type" of movement desired. For example, agents
 * capable of flight would typically need a different controller from those only capable of gravity-bound movement.</p>
 *
 * @implSpec Controller instances should be bound to exactly one {@link Agent}.
 */
public interface Controller {
    /**
     * The current x-coordinate of the {@link Agent} controlled by this controller.
     * @return a {@code double} representing the precise x-coordinate
     */
    double getX();

    /**
     * The current y-coordinate of the {@link Agent} controlled by this controller.
     * @return a {@code double} representing the precise y-coordinate
     */
    double getY();

    /**
     * The current z-coordinate of the {@link Agent} controlled by this controller.
     * @return a {@code double} representing the precise z-coordinate
     */
    double getZ();

    /**
     * Called to advance Agents along a path. This is expected to be called every tick by a {@link Navigator}.
     * @param current the node the controller is currently in
     * @param target the node the controller is attempting to move to
     */
    void advance(@NotNull Node current, @NotNull Node target);

    /**
     * Can be used to determine if this controller has caused its entity to undergo a multi-tick movement (and is thus
     * under "control" for this tick, and possibly more). Useful for navigators to determine when to perform certain
     * checks.
     * @return {@code true} if this controller is "controlling" this entity for this tick, {@code false} otherwise
     */
    boolean hasControl();
}
