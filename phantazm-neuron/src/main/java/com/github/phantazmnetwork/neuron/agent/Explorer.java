package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for determining which nodes may be traversed by an {@link Agent}.
 */
@FunctionalInterface
public interface Explorer {
    /**
     * Produces an {@link Iterable} over {@link Vec3I} instances which may be added to {@code current} to produce new
     * nodes (which will typically be explored by a {@link PathOperation}). The iterable may actively perform
     * calculations as it is iterated. It may also be returned from a cache.
     * @param current the node from which to search for new nodes to expand
     * @return an Iterable which will iterate over the new nodes
     */
    @NotNull Iterable<Vec3I> walkVectors(@NotNull Node current);
}
