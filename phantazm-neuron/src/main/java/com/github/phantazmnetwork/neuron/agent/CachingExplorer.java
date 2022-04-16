package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathCache;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

/**
 * A general {@link Explorer} implementation that takes advantage of a {@link PathCache} object for caching.
 */
public abstract class CachingExplorer implements Explorer {
    //useful for debug purposes
    private static final boolean CACHE_ENABLED = false;

    private final PathCache cache;
    private final String id;

    /**
     * Creates a new ContextualExplorer from the given {@link PathCache} and {@link Descriptor}.
     * @param cache the PathCache instance used for caching
     * @param id the id of the agent performing the exploration
     */
    public CachingExplorer(@NotNull PathCache cache, @NotNull String id) {
        this.cache = Objects.requireNonNull(cache, "context");
        this.id = Objects.requireNonNull(id, "id");
    }

    @Override
    public final @NotNull Iterable<Vec3I> walkVectors(@NotNull Node current) {
        if(CACHE_ENABLED) {
            Vec3I currentPos = current.getPosition();
            return cache.getSteps(currentPos, id).orElse(() -> cache.watchSteps(currentPos, id,
                    getWalkIterator(current)));
        }
        else {
            return () -> getWalkIterator(current);
        }
    }

    /**
     * Returns an {@link Iterator} that calculates each translation vector as it is iterated.
     * @param current the current node
     * @return an Iterator over the translation vectors
     */
    protected abstract @NotNull Iterator<Vec3I> getWalkIterator(@NotNull Node current);
}
