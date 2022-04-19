package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathCache;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A standard {@link Explorer} implementation that attempts to "move" in every direction specified by a given
 * {@link Vec3I} iterator. This class extends {@link CachingExplorer} and therefore takes advantage of the caching
 * functionality provided by {@link PathCache}.
 */
public class TranslationExplorer extends CachingExplorer {
    private static final Iterable<Vec3I> WALK_VECTORS = List.of(
            Vec3I.of(1, 0, 0),
            Vec3I.of(0, 0, 1),
            Vec3I.of(-1, 0, 0),
            Vec3I.of(0, 0, -1),

            Vec3I.of(1, 0, 1),
            Vec3I.of(-1, 0, 1),
            Vec3I.of(-1, 0, 1),
            Vec3I.of(-1, 0, -1),

            Vec3I.of(0, 1, 0)
    );

    private final NodeTranslator translator;
    private final Iterable<Vec3I> vectors;

    /**
     * Creates a new TranslationExplorer which will use the given {@link NodeTranslator}.
     * @param cache the cache used to store computed translation vectors, if null caching will be disabled (not
     *              recommended)
     * @param id the id of the agent using this explorer
     * @param translator the translator used by this explorer
     * @param vectors the walk vectors to explore
     */
    public TranslationExplorer(@Nullable PathCache cache, @NotNull String id, @NotNull NodeTranslator translator,
                               @NotNull Iterable<Vec3I> vectors) {
        super(cache, id);
        this.translator = Objects.requireNonNull(translator, "translator");
        this.vectors = Objects.requireNonNull(vectors, "vectors");
    }

    public TranslationExplorer(@Nullable PathCache cache, @NotNull String id, @NotNull NodeTranslator translator) {
        this(cache, id, translator, WALK_VECTORS);
    }

    @Override
    public void initializeNode(@NotNull Node node) {
        translator.computeOffset(node);
    }

    @Override
    public @NotNull Iterator<Vec3I> getWalkIterator(@NotNull Node current) {
        return Pipe.from(vectors.iterator()).map(delta -> translator.translate(current, delta.getX(), delta.getY(),
                delta.getZ())).filter(delta -> !delta.equals(Vec3I.ORIGIN));
    }
}