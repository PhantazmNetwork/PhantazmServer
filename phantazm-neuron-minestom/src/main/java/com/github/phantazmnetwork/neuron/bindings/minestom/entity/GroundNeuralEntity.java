package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GroundNeuralEntity extends NeuralEntity {
    public GroundNeuralEntity(@NotNull GroundMinestomDescriptor entityType, @NotNull UUID uuid,
                              @NotNull ContextProvider contextProvider) {
        super(entityType, uuid, contextProvider);
    }

    @Override
    public @NotNull NodeTranslator getTranslator(@NotNull Instance instance, @NotNull PathContext context) {
        return new GroundTranslator(context.getCollider(), (GroundMinestomDescriptor) getDescriptor());
    }
}
