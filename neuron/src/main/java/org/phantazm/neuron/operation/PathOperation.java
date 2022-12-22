package org.phantazm.neuron.operation;

import org.jetbrains.annotations.NotNull;
import org.phantazm.neuron.agent.Agent;

/**
 * Represents a potentially ongoing pathfinding operation. Implementations are typically responsible for computing
 * iterations of a particular pathfinding algorithm, such as A*.
 */
public interface PathOperation {
    /**
     * Computes a single iteration of the pathfinding algorithm used by this operation, so long as the operation has not
     * yet completed.
     *
     * @throws IllegalStateException if the state of this operation is not {@link State#IN_PROGRESS}
     */
    void step();

    /**
     * Returns the state of this operation. This should be queried to determine if it's safe to call
     * {@link PathOperation#step()} or {@link PathOperation#getResult()}.
     *
     * @return the current state of this operation
     * @see State
     */
    @NotNull State getState();

    /**
     * Returns the {@link PathResult} object representing the completed operation.
     *
     * @return the PathResult
     * @throws IllegalStateException if this operation's state is {@link State#IN_PROGRESS}
     */
    @NotNull PathResult getResult();

    /**
     * Equivalent to {@code getState() != State.IN_PROGRESS}.
     *
     * @return {@code true} if this operation has completed (if its state is {@link State#FAILED} or
     * {@link State#SUCCEEDED}); false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isComplete() {
        return getState() != State.IN_PROGRESS;
    }

    /**
     * Used to represent the current state of the path.
     */
    enum State {
        /**
         * Represents an incomplete operation. It is necessary to call {@link PathOperation#step()} in order to progress
         * the path's computation.
         */
        IN_PROGRESS,

        /**
         * Represents an operation that has completed <i>and</i> successfully reached its destination, however that is
         * defined by the {@link Agent} trying to pathfind.
         */
        SUCCEEDED,

        /**
         * Represents an operation that has completed and failed. This can either mean that the operation has exhausted
         * all possible nodes (in which case, a best-cast path exists) or the pathfinding {@link Agent} was unable to
         * compute a valid starting position. If the latter is true, no best-case path exists.
         */
        FAILED
    }
}
