package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link Descriptor} for agents that are subject to gravity and can jump or fall.
 */
public interface GroundDescriptor extends PhysicalDescriptor {
    Iterable<? extends Vec3I> DEFAULT_WALK_DIRECTIONS = List.of(
            Vec3I.of(1, 0, 0),
            Vec3I.of(0, 0, 1),
            Vec3I.of(-1, 0, 0),
            Vec3I.of(0, 0, -1),

            Vec3I.of(1, 0, 1),
            Vec3I.of(-1, 0, 1),
            Vec3I.of(1, 0, -1),
            Vec3I.of(-1, 0, -1)
    );

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

    @Override
    default @NotNull Iterable<? extends Vec3I> stepDirections() {
        return DEFAULT_WALK_DIRECTIONS;
    }
}
