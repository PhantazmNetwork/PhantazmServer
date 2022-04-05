package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroundNeuralEntity extends NeuralEntity {
    public GroundNeuralEntity(@NotNull GroundMinestomDescriptor entityType, @NotNull UUID uuid,
                              @NotNull ContextProvider contextProvider) {
        super(entityType, uuid, contextProvider);
        ExecutorService t = Executors.newFixedThreadPool(4);
    }

    @Override
    public @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context) {
        return new GroundTranslator(context.getCollider(), (GroundMinestomDescriptor) getDescriptor());
    }
}
