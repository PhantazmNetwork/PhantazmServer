package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.Nullable;

/**
 * A basic PathResult implementation, representing a completed (successful or failed) path.
 */
//TODO: make this, and some similarly simple data containers, records?
@SuppressWarnings("ClassCanBeRecord")
public class BasicResult implements PathResult {
    private final Node path;
    private final boolean successful;

    /**
     * Creates a new BasicResult.
     * @param path the starting node (nullable)
     * @param successful if the path is successful
     */
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
