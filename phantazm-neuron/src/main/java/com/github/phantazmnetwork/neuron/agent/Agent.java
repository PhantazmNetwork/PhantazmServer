package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.Destination;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something capable of pathfinding. This is the most <i>general</i> representation of a navigation-capable
 * object, and generally all that is required to be used by a pathfinding algorithm such as A*. More specific
 * sub-interfaces exist to expose more complex functionality.
 * @see PhysicalAgent
 * @see GroundAgent
 */
public interface Agent {
    @NotNull Vec3I getStartPosition();

    @NotNull Walker getWalker();

    @NotNull Calculator getCalculator();

    boolean reachedDestination(int x, int y, int z, @NotNull Destination destination);
}