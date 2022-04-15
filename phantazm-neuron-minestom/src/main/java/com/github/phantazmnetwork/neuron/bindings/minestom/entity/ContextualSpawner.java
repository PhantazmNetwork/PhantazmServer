package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class ContextualSpawner implements Spawner {
    private final ContextProvider contextProvider;

    public ContextualSpawner(@NotNull ContextProvider contextProvider) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider");
    }

    @Override
    public <TType extends MinestomDescriptor, TReturn extends NeuralEntity> @NotNull TReturn spawnEntity(
            @NotNull Instance instance, @NotNull Point point, @NotNull TType type,
            @NotNull NeuralEntityFactory<TType, TReturn> factory, @NotNull Consumer<TReturn> settings) {
        TReturn entity = factory.build(type, UUID.randomUUID(), contextProvider);
        settings.accept(entity);
        entity.setInstance(instance, point);
        return entity;
    }
}
