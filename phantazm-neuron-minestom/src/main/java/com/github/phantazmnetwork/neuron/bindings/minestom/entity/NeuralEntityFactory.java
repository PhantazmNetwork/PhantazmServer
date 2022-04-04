package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@FunctionalInterface
public interface NeuralEntityFactory<TType extends MinestomDescriptor, TReturn extends NeuralEntity> {
    @NotNull TReturn build(@NotNull TType type, @NotNull UUID uuid, @NotNull ContextProvider provider);
}
