package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents a spawner of {@link NeuralEntity}s.
 */
@FunctionalInterface
public interface Spawner {
    /**
     * Spawns a new {@link NeuralEntity} in the provided {@link Instance}.
     * @param instance the instance to spawn the entity in
     * @param point the location at which to place the entity in the instance
     * @param type a descriptor for the new entity
     * @param factory a {@link NeuralEntityFactory} used to create the entity
     * @param settings a {@link Consumer} used to apply arbitrary modifications to the entity just before it is spawned
     *                 (before its instance is set)
     * @param <TDescriptor> the descriptor type
     * @param <TEntity> the entity type
     * @return a freshly-spawned NeuralEntity with its instance set
     */
    <TDescriptor extends MinestomDescriptor, TEntity extends NeuralEntity> @NotNull TEntity spawnEntity(
            @NotNull Instance instance, @NotNull Point point, @NotNull TDescriptor type,
            @NotNull NeuralEntityFactory<? super TDescriptor, ? extends TEntity> factory,
            @NotNull Consumer<? super TEntity> settings);

    /**
     * Convenience overload for {@code spawnEntity} that does not apply any settings.
     * @param instance the instance to spawn the entity in
     * @param point the location at which to place the entity in the instance
     * @param type a descriptor for the new entity
     * @param factory a {@link NeuralEntityFactory} used to create the entity
     * @param <TDescriptor> the descriptor type
     * @param <TEntity> the entity type
     * @return a freshly-spawned NeuralEntity with its instance set
     */
    default <TDescriptor extends MinestomDescriptor, TEntity extends NeuralEntity> @NotNull TEntity spawnEntity(
            @NotNull Instance instance, @NotNull Point point, @NotNull TDescriptor type,
            @NotNull NeuralEntityFactory<? super TDescriptor, ? extends TEntity> factory) {
        return spawnEntity(instance, point, type, factory, ignored -> {});
    }
}
