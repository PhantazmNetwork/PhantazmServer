package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

/**
 * Container for information that can be shared between many agents. Encapsulates a cache that agents can query in
 * order to avoid redundant collision checks.
 */
public interface PathCache {
    /**
     * Attempts to retrieve a cached {@link Iterable} of translation vectors for the given descriptor ID.
     * @param origin the origin vector
     * @param id the descriptor ID
     * @return an {@link Optional} which may contain an {@link Iterable} over the cached translation vectors, or
     * {@link Optional#empty()} if there is a cache miss
     * @see Descriptor
     */
    @NotNull Optional<Iterable<Vec3I>> getSteps(@NotNull Vec3I origin, @NotNull String id);

    /**
     * Produces an {@link Iterator} that will iterate over the provided iterator while recording the values it
     * produces, possibly caching them when the iterator has completed.
     * @param origin the origin vector
     * @param id the descriptor ID
     * @param steps the iterator to record
     * @return an Iterator which wraps the provided iterator, while additionally recording the values it produces and
     * possibly caching them
     */
    @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull String id,
                                        @NotNull Iterator<Vec3I> steps);

    /**
     * Invalidates (removes) any values associated with the given origin vector.
     * @param origin the origin value to invalidate
     */
    void invalidateOrigin(@NotNull Vec3I origin);

    /**
     * Invalidates (removes) all values associated with the given origin vectors.
     * @param steps an {@link Iterable} over the origin vectors that will be invalidated
     */
    void invalidateOrigins(@NotNull Iterable<? extends Vec3I> steps);

    /**
     * Invalidates every entry in the cache.
     */
    void invalidateAll();
}
