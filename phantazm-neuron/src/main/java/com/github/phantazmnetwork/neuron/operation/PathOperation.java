package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;

public interface PathOperation {
    enum State {
        IN_PROGRESS,
        SUCCEEDED,
        FAILED
    }

    void step();

    @NotNull State getState();

    @NotNull PathContext getContext();

    @NotNull PathResult getResult();

    default boolean isComplete() {
        return getState() != State.IN_PROGRESS;
    }
}
