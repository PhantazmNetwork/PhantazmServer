package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class BasicSpawner implements Spawner {
    private final ContextProvider contextProvider;

    public BasicSpawner(@NotNull ContextProvider contextProvider) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider");
    }

    @Override
    public <TType extends MinestomDescriptor, TReturn extends NeuralEntity> @NotNull TReturn spawnEntity(
            @NotNull Instance instance, @NotNull Point point, @NotNull TType type,
            @NotNull NeuralEntityFactory<TType, TReturn> factory) {
        TReturn entity = factory.build(type, UUID.randomUUID(), contextProvider);
        entity.setInstance(instance, point);
        return entity;
    }
}
