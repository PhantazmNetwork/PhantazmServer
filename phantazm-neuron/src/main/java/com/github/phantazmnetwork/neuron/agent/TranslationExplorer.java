package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import com.github.phantazmnetwork.commons.iterator.EnhancedIterator;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathCache;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import org.jetbrains.annotations.NotNull;

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
     * Creates a new GroundExplorer which will use the given {@link NodeTranslator}.
     * @param cache the cache used to store computed translation vectors
     * @param id the id of the agent using this explorer
     * @param translator the translator used by this explorer
     * @param vectors the walk vectors to explore
     */
    public TranslationExplorer(@NotNull PathCache cache, @NotNull String id, @NotNull NodeTranslator translator,
                               @NotNull Iterable<Vec3I> vectors) {
        super(cache, id);
        this.translator = Objects.requireNonNull(translator, "translator");
        this.vectors = Objects.requireNonNull(vectors, "vectors");
    }

    public TranslationExplorer(@NotNull PathCache cache, @NotNull String id, @NotNull NodeTranslator translator) {
        this(cache, id, translator, WALK_VECTORS);
    }

    @Override
    public @NotNull Iterator<Vec3I> getWalkIterator(@NotNull Node current) {
        Vec3I currentPos = current.getPosition();
        int x = currentPos.getX();
        int y = currentPos.getY();
        int z = currentPos.getZ();

        Node parentNode = current.getParent();

        Vec3I parentPos = parentNode == null ? null : parentNode.getPosition();
        return EnhancedIterator.adapt(vectors.iterator()).filter(delta -> parentPos == null || !Vec3I.equals(parentPos
                        .getX(), parentPos.getY(), parentPos.getZ(), x + delta.getX(), y + delta.getY(), z +
                        delta.getZ())).map(delta -> translator.translate(x, y, z, delta.getX(), delta.getY(), delta
                .getZ())).filter(delta -> !delta.equals(Vec3I.ORIGIN));
    }
}