package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The result of a pathfinding operation, representing a completed (successful or failed) path.
 */
public record PathResult(@NotNull Node getStart, int exploredCount, boolean isSuccessful) {
    /**
     * Creates a new BasicResult.
     *
     * @param getStart the starting node
     * @param exploredCount the number of nodes that were explored for this path
     * @param isSuccessful if the path is successful
     */
    public PathResult {
        Objects.requireNonNull(getStart, "getStart");
    }
}
