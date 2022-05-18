package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;
import com.github.phantazmnetwork.neuron.engine.PathCache;

/**
 * Describes the characteristics (type) of an agent. Many agents may share a single descriptor. Descriptors encapsulate
 * the pathfinding behavior of agents — agents with identical descriptors will exhibit identical behavior.
 */
public interface Descriptor {
    /**
     * Returns the identification string used by this descriptor. Agents whose descriptors have equal ids may be able to
     * access the same cached values for certain operations.
     * @return the ID string for this descriptor
     */
    @NotNull String getID();

    /**
     * Returns the {@link Calculator} used to compute distance/heuristic values for this agent.
     * @return the Calculator instance which should be used by this agent
     */
    @NotNull Calculator getCalculator();

    /**
     * Determines if the agent has completed pathfinding. The most basic implementation is to simply check if
     * {@code position} and {@code destination} are equal. More complex conditions may be given, but care should be
     * taken to ensure that they are correlated with the heuristic used
     * ({@link Calculator#heuristic(int, int, int, int, int, int)}).
     * @param position the current position
     * @param destination the destination position
     * @return whether this agent should have completed
     */
    boolean isComplete(@NotNull Vec3I position, @NotNull Vec3I destination);

    /**
     * <p>Determines if the cache entry starting at {@code origin} should be invalidated if an update occurred at
     * location {@code update}.</p>
     *
     * <p>In order to avoid cases where stale values lead to incorrect pathfinding, this method should never return
     * false if the update would impact pathfinding. However, returning {@code false} will lead to cache retention,
     * which can improve performance, and should (but is not required to be) done as frequently as possible.</p>
     *
     * <p>Any detailed checks performed should be significantly faster than performing collision, in order to see any
     * benefits from caching.</p>
     *
     * <p>The default implementation makes no assumptions about the environment, and always returns {@code true}.</p>
     * @param origin the agent origin
     * @param update the location of the update
     * @param oldSolid the solid that was previously located at the given position
     * @param newSolid the new solid located at the given position
     * @return true if the cache should be invalidated, false if it should be retained
     * @see PathCache
     */
    default boolean shouldInvalidate(@NotNull Vec3I origin, @NotNull Vec3I update, @NotNull Solid oldSolid,
                                     @NotNull Solid newSolid) {
        return true;
    }

    @NotNull Iterable<? extends Vec3I> stepDirections();
}
