package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public abstract class ContextualExplorer implements Explorer {
    private final PathContext context;
    private final int descriptor;

    public ContextualExplorer(@NotNull PathContext context, int descriptor) {
        this.context = Objects.requireNonNull(context, "context");
        this.descriptor = descriptor;
    }

    @Override
    public final @NotNull Iterable<? extends Vec3I> walkVectors(@NotNull Node current) {
        Vec3I currentPos = current.getPosition();
        Iterable<? extends Vec3I> cached = context.getStep(descriptor, currentPos);
        if(cached != null) {
            return cached;
        }

        return () -> context.watchSteps(descriptor, currentPos, getWalkIterator(current));
    }


    protected abstract @NotNull Iterator<? extends Vec3I> getWalkIterator(@NotNull Node current);
}
