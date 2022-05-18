package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

/**
 * Container for information that can be shared between many agents. Encapsulates a cache that agents can query in
 * order to avoid redundant collision checks.
 */
public interface PathCache {
    /**
     * Attempts to retrieve a cached {@link Iterable} of translation vectors for the given descriptor.
     * @param origin the origin vector
     * @param descriptor the descriptor
     * @return an {@link Optional} which may contain an {@link Iterable} over the cached translation vectors, or
     * {@link Optional#empty()} if there is a cache miss
     * @see Descriptor
     */
    @NotNull Optional<Iterable<Vec3I>> getSteps(@NotNull Vec3I origin, @NotNull Descriptor descriptor);

    /**
     * Produces an {@link Iterator} that will iterate over the provided iterator while recording the values it
     * produces, possibly caching them when the iterator has completed.
     * @param origin the origin vector
     * @param descriptor the descriptor
     * @param steps the iterator to record
     * @return an Iterator which wraps the provided iterator, while additionally recording the values it produces and
     * possibly caching them
     */
    @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull Descriptor descriptor,
                                        @NotNull Iterator<Vec3I> steps);

    /**
     * Called when a solid is updated at the given location. Invalidates the appropriate entries in the internal cache.
     * @param location the location of the update
     */
    void handleUpdate(@NotNull Vec3I location, @NotNull Solid oldSolid, @NotNull Solid newSolid);
}
