package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Responsible for determining which nodes may be traversed by an {@link Agent}.
 */
public interface Explorer {
    /**
     * An immutable list of vectors representing the standard movement options available to walking agents.
     */
    Iterable<Vec3I> WALK_VECTORS = List.of(
            Vec3I.of(1, 0, 0),
            Vec3I.of(-1, 0, 0),
            Vec3I.of(0, 0, 1),
            Vec3I.of(0, 0, -1),
            Vec3I.of(1, 0, 1),
            Vec3I.of(1, 0, -1),
            Vec3I.of(-1, 0, 1),
            Vec3I.of(-1, 0, -1),
            Vec3I.of(0, 1, 0)
    );

    /**
     * Produces an {@link Iterable} over {@link Vec3I} instances which may be added to {@code current} to produce new
     * nodes (which will typically be explored by a {@link PathOperation}).
     * @param current the node from which to search for new nodes to expand
     * @return an Iterable which will iterate over the new nodes
     */
    @NotNull Iterable<? extends Vec3I> walkVectors(@NotNull Node current);
}
