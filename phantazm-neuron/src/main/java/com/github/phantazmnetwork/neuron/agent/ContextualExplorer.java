package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public abstract class ContextualExplorer implements Explorer {
    private final PathContext context;
    private final Agent.Descriptor descriptor;

    public ContextualExplorer(@NotNull PathContext context, @NotNull Agent.Descriptor descriptor) {
        this.context = Objects.requireNonNull(context, "context");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public final @NotNull Iterable<Vec3I> walkVectors(@NotNull Node current) {
        Vec3I currentPos = current.getPosition();
        return context.getStep(currentPos, descriptor).orElseGet(() -> () -> context.watchSteps(currentPos, descriptor,
                getWalkIterator(current)));

    }

    protected abstract @NotNull Iterator<Vec3I> getWalkIterator(@NotNull Node current);
}
