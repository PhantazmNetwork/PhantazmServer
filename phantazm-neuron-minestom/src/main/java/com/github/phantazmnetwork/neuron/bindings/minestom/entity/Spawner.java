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
     *
     * @param instance the instance to spawn the entity in
     * @param point    the location at which to place the entity in the instance
     * @param type     a descriptor for the new entity
     * @param settings a {@link Consumer} used to apply arbitrary modifications to the entity just before it is spawned
     *                 (before its instance is set)
     * @return a freshly-spawned NeuralEntity with its instance set
     */
    @NotNull NeuralEntity spawnEntity(@NotNull Instance instance, @NotNull Point point,
                                      @NotNull MinestomDescriptor type,
                                      @NotNull Consumer<? super NeuralEntity> settings);

    /**
     * Convenience overload for {@code spawnEntity} that does not apply any settings.
     *
     * @param instance the instance to spawn the entity in
     * @param point    the location at which to place the entity in the instance
     * @param type     a descriptor for the new entity
     * @return a freshly-spawned NeuralEntity with its instance set
     */
    default @NotNull NeuralEntity spawnEntity(@NotNull Instance instance, @NotNull Point point,
                                              @NotNull MinestomDescriptor type) {
        return spawnEntity(instance, point, type, ignored -> {
        });
    }
}
