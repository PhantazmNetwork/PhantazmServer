package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface Spawner {
    <TType extends MinestomDescriptor, TReturn extends NeuralEntity> @NotNull TReturn spawnEntity(
            @NotNull Instance instance, @NotNull Point point, @NotNull TType type,
            @NotNull NeuralEntityFactory<? super TType, ? extends TReturn> factory,
            @NotNull Consumer<? super TReturn> settings);
}
