package org.phantazm.neuron.navigator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.neuron.node.Node;
import org.phantazm.neuron.operation.PathResult;

/**
 * <p>Represents an object that handles (listens to) various events that can occur during navigation. This is useful for
 * logging, debugging, and keeping statistics.</p>
 *
 * <p>In general, the methods of this class should not modify any {@link Navigator} instances passed to them.</p>
 */
public interface NavigationTracker {
    /**
     * The {@code null} NavigationTracker, whose methods perform no operation.
     */
    NavigationTracker NULL = new NavigationTracker() {
        @Override
        public void onPathfind(@NotNull Navigator navigator) {
        }

        @Override
        public void onPathfindComplete(@NotNull Navigator navigator, @NotNull Node pathStart,
                @Nullable PathResult result) {
        }

        @Override
        public void onDestinationReached(@NotNull Navigator navigator) {
        }

        @Override
        public void onNavigationError(@NotNull Navigator navigator, @Nullable Node pathStart,
                @NotNull ErrorType errorType) {
        }
    };

    /**
     * Called directly after the given {@link Navigator} begins a pathfinding operation. The navigator should have a set
     * destination.
     *
     * @param navigator the navigator which just began pathfinding
     */
    void onPathfind(@NotNull Navigator navigator);

    /**
     * Called directly after the given {@link Navigator} finishes a pathfinding operation (but before it begins walking
     * along it). The navigator should have a set destination.
     *
     * @param navigator the navigator which just finished a pathfinding operation
     * @param pathStart the beginning of the path
     * @param result    the {@link PathResult} representing the completed pathfinding operation, which may be null if the
     *                  operation was cancelled or an error occurred
     */
    void onPathfindComplete(@NotNull Navigator navigator, @NotNull Node pathStart, @Nullable PathResult result);

    /**
     * Called directly after the given {@link Navigator} reaches its destination (but before a new destination is
     * calculated). Therefore, the navigator should have a destination set.
     *
     * @param navigator the navigator which just reached its destination
     */
    void onDestinationReached(@NotNull Navigator navigator);

    /**
     * Called when the given {@link Navigator} encounters a navigation error.
     *
     * @param navigator the navigator which just erred
     * @param pathStart the beginning of the path
     * @param errorType the {@link ErrorType} describing the error
     */
    void onNavigationError(@NotNull Navigator navigator, @Nullable Node pathStart, @NotNull ErrorType errorType);

    /**
     * Describes types of navigation error.
     */
    enum ErrorType {
        /**
         * An ErrorType representing a "stuck" agent that is trying to move to a new node, but cannot.
         */
        STUCK,

        /**
         * An ErrorType representing an agent which failed to find an appropriate starting node for its current path.
         */
        NO_START
    }
}
