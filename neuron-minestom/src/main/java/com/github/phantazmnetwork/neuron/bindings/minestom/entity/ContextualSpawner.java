package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A {@link Spawner} implementation that uses a single {@link ContextProvider} to build and spawn all entities.
 */
public class ContextualSpawner implements Spawner {
    private final ContextProvider contextProvider;

    /**
     * Creates a new ContextualSpawner that will use the given {@link ContextProvider} to build all entities.
     *
     * @param contextProvider the ContextProvider which will be used to construct new entities
     */
    public ContextualSpawner(@NotNull ContextProvider contextProvider) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider");
    }

    @Override
    public @NotNull NeuralEntity spawnEntity(@NotNull Instance instance, @NotNull Point point,
                                             @NotNull MinestomDescriptor type,
                                             @NotNull Consumer<? super NeuralEntity> settings) {
        NeuralEntity entity = new NeuralEntity(type, UUID.randomUUID(), contextProvider);
        settings.accept(entity);
        entity.setInstance(instance, point);
        return entity;
    }
}
