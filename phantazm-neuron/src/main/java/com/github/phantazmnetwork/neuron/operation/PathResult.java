package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the result of a pathfinding operation.
 */
public interface PathResult {
    /**
     * Returns {@code true} if the operation completed successfully (found its goal), returns {@code false} if it failed
     * to find the goal, or if the agent did not provide a starting coordinate (is not valid for pathfinding).
     * @return {@code true} if the operation completed successfully, {@code false} otherwise
     */
    boolean isSuccessful();

    /**
     * <p>Retrieves the {@link Node} representing the start of this path, or null if no path could be calculated. All
     * PathResults for which {@link PathResult#isSuccessful()} returns {@code true} will return a non-null node here.
     * If the result is <i>not</i> successful the node will only be null if the agent attempting to pathfind determined
     * it could not supply an initial node (in other words, if it is not valid for pathfinding).</p>
     *
     * <p>Since Node implements {@link Iterable}, one can iterate from the start of the path to the end of the path,
     * which will generally either lead to the destination itself or somewhere close to it if it is unreachable.</p>
     * @return a Node object representing a path
     */
    Node getPath();
}
