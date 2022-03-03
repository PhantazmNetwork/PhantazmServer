package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;

public interface PathResult extends Iterable<Node> {
    @NotNull PathOperation getOperation();
}
