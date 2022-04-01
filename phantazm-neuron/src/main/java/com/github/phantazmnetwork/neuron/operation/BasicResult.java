package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A basic PathResult implementation, representing a completed (successful or failed) path.
 */
public record BasicResult(@NotNull Node getPath, boolean isSuccessful) implements PathResult {
    /**
     * Creates a new BasicResult.
     *
     * @param getPath the starting node
     * @param isSuccessful if the path is successful
     */
    public BasicResult {
        Objects.requireNonNull(getPath, "getPath");
    }
}
