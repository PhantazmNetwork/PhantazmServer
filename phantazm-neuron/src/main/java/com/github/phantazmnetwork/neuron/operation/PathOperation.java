package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;

public interface PathOperation {
    enum State {
        IN_PROGRESS,
        SUCCEEDED,
        FAILED
    }

    void step(@NotNull PathContext context);
}
