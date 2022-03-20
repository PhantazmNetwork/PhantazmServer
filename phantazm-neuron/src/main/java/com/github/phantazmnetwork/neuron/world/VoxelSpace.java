package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import org.jetbrains.annotations.NotNull;

public abstract class VoxelSpace implements Space {
    @Override
    public @NotNull Iterable<? extends Solid> solidsOverlapping(double oX, double oY, double oZ, double vX, double vY,
                                                                double vZ) {
        return () -> new AdvancingIterator<Solid>() {
            @Override
            public boolean advance() {
                return false;
            }
        };
    }
}
