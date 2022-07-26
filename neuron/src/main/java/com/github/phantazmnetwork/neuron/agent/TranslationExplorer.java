package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathCache;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

/**
 * A standard {@link Explorer} implementation that attempts to "move" in every direction specified by a given
 * {@link Vec3I} iterator. This class extends {@link CachingExplorer} and therefore takes advantage of the caching
 * functionality provided by {@link PathCache}.
 */
public class TranslationExplorer extends CachingExplorer {
    private final NodeTranslator translator;

    /**
     * Creates a new TranslationExplorer which will use the given {@link NodeTranslator}.
     *
     * @param cache      the cache used to store computed translation vectors, if null caching will be disabled (not
     *                   recommended)
     * @param descriptor the descriptor of the agent using this explorer
     * @param translator the translator used by this explorer
     */
    public TranslationExplorer(@Nullable PathCache cache, @NotNull Descriptor descriptor,
            @NotNull NodeTranslator translator) {
        super(cache, descriptor);
        this.translator = Objects.requireNonNull(translator, "translator");
    }

    @Override
    public void initializeNode(@NotNull Node node) {
        translator.initializeNode(node);
    }

    @Override
    public @NotNull Iterator<Vec3I> getWalkIterator(@NotNull Node current) {
        return Pipe.from(descriptor.stepDirections())
                .map(delta -> translator.translate(current, delta.getX(), delta.getY(), delta.getZ()))
                .filter(delta -> !delta.equals(Vec3I.ORIGIN));
    }
}
