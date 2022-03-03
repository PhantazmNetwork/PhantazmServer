package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;

public interface PathResult {
    @NotNull PathOperation getOperation();

    @NotNull Iterable<Node> getPath();
}
