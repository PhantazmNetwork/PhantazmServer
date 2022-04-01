package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

/**
 * A general {@link Explorer} implementation that takes advantage of a {@link PathContext} object for caching.
 */
public abstract class ContextualExplorer implements Explorer {
    private final PathContext context;
    private final String id;

    /**
     * Creates a new ContextualExplorer from the given {@link PathContext} and {@link Descriptor}.
     * @param context the PathContext instance used for caching
     * @param id the id of the agent performing the exploration
     */
    public ContextualExplorer(@NotNull PathContext context, @NotNull String id) {
        this.context = Objects.requireNonNull(context, "context");
        this.id = Objects.requireNonNull(id, "id");
    }

    @Override
    public final @NotNull Iterable<Vec3I> walkVectors(@NotNull Node current) {
        Vec3I currentPos = current.getPosition();
        return context.getStep(currentPos, id).orElseGet(() -> () -> context.watchSteps(currentPos, id,
                getWalkIterator(current)));
    }

    /**
     * Returns an {@link Iterator} that calculates each translation vector as it is iterated.
     * @param current the current node
     * @return an Iterator over the translation vectors
     */
    protected abstract @NotNull Iterator<Vec3I> getWalkIterator(@NotNull Node current);
}
