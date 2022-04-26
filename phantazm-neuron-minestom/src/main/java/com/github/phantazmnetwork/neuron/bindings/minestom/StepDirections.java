package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.vector.Vec3I;

import java.util.List;

public final class StepDirections {
    private StepDirections() { throw new UnsupportedOperationException(); }

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
