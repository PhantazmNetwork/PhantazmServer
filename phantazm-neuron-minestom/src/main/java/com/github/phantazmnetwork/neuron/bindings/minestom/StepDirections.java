package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;

import java.util.List;

/**
 * Utility class containing common, shared {@link Iterable}s of step directions.
 * @see NeuralEntity
 */
public final class StepDirections {
    private StepDirections() { throw new UnsupportedOperationException(); }

    /**
     * The standard sequence of step directions for gravity-bound agents.
     */
    public static final Iterable<Vec3I> WALK = List.of(
            Vec3I.of(1, 0, 0),
            Vec3I.of(0, 0, 1),
            Vec3I.of(-1, 0, 0),
            Vec3I.of(0, 0, -1),

            Vec3I.of(1, 0, 1),
            Vec3I.of(-1, 0, 1),
            Vec3I.of(1, 0, -1),
            Vec3I.of(-1, 0, -1)
    );
}
