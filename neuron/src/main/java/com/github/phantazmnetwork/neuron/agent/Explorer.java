package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for determining which nodes may be traversed by an {@link Agent}.
 */
public interface Explorer {
    /**
     * Produces an {@link Iterable} over {@link Vec3I} instances which may be added to {@code current} to produce new
     * nodes (which will typically be explored by a {@link PathOperation}). The iterable may actively perform
     * calculations as it is iterated, or return a value from cache.
     *
     * @param current the node from which to search for new nodes to expand
     * @return an Iterable which will iterate over the new nodes
     */
    @NotNull Iterable<Vec3I> expandNode(@NotNull Node current);

    /**
     * Performs implementation-dependent initialization tasks on the given Node object. This may include setting the
     * node's offset height or other properties.
     *
     * @param node the node to initialize
     */
    void initializeNode(@NotNull Node node);
}
