package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GroundNeuralEntity extends NeuralEntity {
    public GroundNeuralEntity(@NotNull GroundNeuralEntityType entityType, @NotNull UUID uuid,
                              @NotNull PathContext context) {
        super(entityType, uuid, context, new GroundTranslator(context.getCollider(), entityType));
    }
}
