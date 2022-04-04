package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Spawner {
    <TType extends NeuralEntityType, TReturn extends NeuralEntity> @NotNull TReturn spawnEntity(
            @NotNull Instance instance, @NotNull TType type, @NotNull NeuralEntityFactory<TType, TReturn> factory);
}
