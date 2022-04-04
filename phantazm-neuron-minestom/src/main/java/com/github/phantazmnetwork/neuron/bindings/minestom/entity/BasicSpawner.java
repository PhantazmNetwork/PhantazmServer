package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BasicSpawner implements Spawner {
    private final ContextProvider contextProvider;

    public BasicSpawner(@NotNull ContextProvider contextProvider) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider");
    }

    @Override
    public <TType extends MinestomDescriptor, TReturn extends NeuralEntity> @NotNull TReturn spawnEntity(
            @NotNull Instance instance, @NotNull TType type, @NotNull NeuralEntityFactory<TType, TReturn> factory) {
        PathContext pathContext = contextProvider.provideContext(instance);
        TReturn entity = factory.build(type, UUID.randomUUID(), pathContext);
        entity.setInstance(instance);
        return entity;
    }
}
