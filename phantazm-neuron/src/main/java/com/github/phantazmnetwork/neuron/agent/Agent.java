package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something capable of pathfinding. This is the most general representation of a navigation-capable object,
 * and generally all that is required to be used by a pathfinding algorithm such as A*. More specific sub-interfaces
 * exist to expose more complex functionality.
 * @see PhysicalAgent
 */
public interface Agent {
    /**
     * Determines if the agent has a starting location. In other words, returns {@code true} if the agent is capable of
     * pathfinding, and {@code false} if it isn't. {@link PathOperation} implementations query this method before
     * pathfinding starts. If no start position exists, the operation immediately terminates in a failed state.
     * @return {@code true} if this agent has a starting position (is valid for pathing); {@code false} otherwise
     */
    boolean hasStartPosition();

    /**
     * <p>Retrieves, and potentially computes, the starting position of this agent. This value may be cached or lazily
     * computed, but it should only expose <i>one</i> unchanging value. The starting position might not reflect the
     * actual position of the agent. In particular, the agent's actual position will typically be a single or
     * double-precision floating point 3D vector, whereas pathfinding only deals with 3D integer vectors.</p>
     *
     * <p>The computation of the starting position may be more complicated than one might expect. It is important to
     * ensure that for any returned vector {@code vec}, there exists an unobstructed path from the agent's actual
     * position to {@code vec}. Naive implementations of this method that do not perform the necessary checks may cause
     * agents to become "stuck" as they attempt to walk to their "starting" node.</p>
     *
     * <p>In some cases, the agent may be unable to produce a meaningful starting position. This can happen if the
     * agent's position is invalid (what this means is up to the implementation), or if world conditions are such that
     * there is no way to determine the starting position. In these cases, this method should throw an
     * {@link IllegalStateException}.</p>
     *
     * <p>Before calling this method, users must query {@link Agent#hasStartPosition()} to determine if a starting point
     * exists, and by extension determine if this agent is valid for pathfinding.</p>
     * @return the starting position of this entity, which should be immutable
     * @throws IllegalStateException if no start position may be computed; only if {@link Agent#hasStartPosition()}
     * returns false
     */
    @NotNull Vec3I getStartPosition();
}