package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PhantazmMob(@NotNull MobModel model,
                          @NotNull NeuralEntity entity) {

    public PhantazmMob {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(entity, "entity");
    }

}
