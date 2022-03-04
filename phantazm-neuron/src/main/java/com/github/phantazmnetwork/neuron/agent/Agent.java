package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something capable of pathfinding. Provides methods for determining starting position, if a space may be
 * traversed without colliding, computing values for A*, and checking if the agent's goal has been reached.
 */
public interface Agent {
    @NotNull Vec3I computeStartPosition();

    @NotNull Walker getWalker();

    @NotNull Calculator getCalculator();

    boolean reachedDestination(int x, int y, int z, int destX, int destY, int destZ);
}