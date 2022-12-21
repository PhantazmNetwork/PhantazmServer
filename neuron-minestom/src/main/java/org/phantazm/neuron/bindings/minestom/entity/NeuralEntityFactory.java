package org.phantazm.neuron.bindings.minestom.entity;

import org.jetbrains.annotations.NotNull;
import org.phantazm.neuron.bindings.minestom.ContextProvider;
import org.phantazm.neuron.engine.PathContext;

import java.util.UUID;

/**
 * Represents a supplier of {@link NeuralEntity} instances.
 *
 * @param <TDescriptor> the descriptor type
 * @param <TEntity>     the entity type
 */
@FunctionalInterface
public interface NeuralEntityFactory<TDescriptor extends MinestomDescriptor, TEntity extends NeuralEntity> {
    /**
     * Builds a new {@link NeuralEntity}. Its instance will be initially un-set and must be set in order to actually
     * spawn.
     *
     * @param type     a descriptor describing the desired entity
     * @param uuid     a unique id for the entity
     * @param provider the {@link ContextProvider} used to supply {@link PathContext} instances for this entity
     * @return a new NeuralEntity implementation
     */
    @NotNull TEntity build(@NotNull TDescriptor type, @NotNull UUID uuid, @NotNull ContextProvider provider);
}
