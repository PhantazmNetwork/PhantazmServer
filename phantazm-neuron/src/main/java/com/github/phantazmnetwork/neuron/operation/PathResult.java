package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;

import java.util.Objects;

/**
 * A basic PathResult implementation, representing a completed (successful or failed) path.
 */
public record PathResult(Node getPath, boolean isSuccessful) {
    /**
     * Creates a new BasicResult.
     *
     * @param getPath the starting node
     * @param isSuccessful if the path is successful
     */
    public PathResult {
        Objects.requireNonNull(getPath, "getPath");
    }
}
