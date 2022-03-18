package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.Nullable;

public class BasicResult implements PathResult {
    private final Node path;
    private final boolean successful;

    public BasicResult(@Nullable Node path, boolean successful) {
        this.path = path;
        this.successful = path != null && successful;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public Node getPath() {
        return path;
    }
}
