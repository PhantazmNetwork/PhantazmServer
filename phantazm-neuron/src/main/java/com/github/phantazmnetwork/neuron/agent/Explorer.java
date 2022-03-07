package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface Explorer {
    @NotNull Iterable<? extends Vec3I> walkVectors(@NotNull Node current);
}
