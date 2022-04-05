package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.AdvancingIterator;
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
    private static final Iterable<? extends Vec3I> WALK_VECTORS = List.of(
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
    private final Iterable<? extends Vec3I> vectors;

    /**
     * Creates a new GroundExplorer which will use the given {@link NodeTranslator}.
     * @param cache the cache used to store computed translation vectors
     * @param id the id of the agent using this explorer
     * @param translator the translator used by this explorer
     * @param vectors the walk vectors to explore
     */
    public TranslationExplorer(@NotNull PathCache cache, @NotNull String id, @NotNull NodeTranslator translator,
                               @NotNull Iterable<? extends Vec3I> vectors) {
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

        //capture ref to simple vec3I rather than parent (which as it is a Node object, may have a big ref chain)
        Vec3I parentPos = parentNode == null ? null : parentNode.getPosition();

        //use AdvancingIterator to reduce memory footprint; we don't need to actually store nodes in a collection
        return new AdvancingIterator<>() {
            private final Iterator<? extends Vec3I> walkIterator = vectors.iterator();

            @Override
            public boolean advance() {
                while (walkIterator.hasNext()) {
                    Vec3I delta = walkIterator.next();

                    /*
                    avoid obvious cases of backtracking with a simple check. although properly-implemented pathfinding
                    algorithms will give the correct path even if we allow backtracking, we should still try to avoid
                    performing collision checks if we don't have to
                     */
                    if(parentPos == null || !Vec3I.equals(parentPos.getX(), parentPos.getY(), parentPos.getZ(), x +
                                    delta.getX(), y + delta.getY(), z + delta.getZ())) {
                        //this might be (0, 0, 0), which indicates our walk delta is not traversable
                        Vec3I newDelta = translator.translate(x, y, z, delta.getX(), delta.getY(), delta.getZ());

                        /*
                        only explore the next node if our delta is non-zero (in other words, if we're going to get a new
                        node)
                         */
                        if(!newDelta.equals(Vec3I.ORIGIN)) {
                            super.value = newDelta;
                            return true;
                        }
                    }
                }

                return false;
            }
        };
    }
}
