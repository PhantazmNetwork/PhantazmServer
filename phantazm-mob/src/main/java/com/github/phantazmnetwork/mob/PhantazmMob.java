package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A representation of a custom mob in Phantazm.
 * @param model The model for the mob
 * @param entity The actual {@link NeuralEntity} instance of the mob
 */
public record PhantazmMob(@NotNull MobModel model, @NotNull NeuralEntity entity) {

    /**
     * Creates a PhantazmMob instance
     * @param model The model for the mob
     * @param entity The actual {@link NeuralEntity} instance of the mob
     */
    public PhantazmMob {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(entity, "entity");
    }

}
