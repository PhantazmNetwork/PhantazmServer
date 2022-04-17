package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a source of {@link Solid} objects located at a specific coordinate within a chunk.
 */
@FunctionalInterface
public interface SolidSource {
    /**
     * Obtains the solid located at the given coordinates, if present.
     * @param x the x-coordinate, must be in range [0, 16)
     * @param y the y-coordinate, must be in range [0, 16,777,216)
     * @param z the z-coordinate, must be in range [0, 16)
     * @return the {@link Solid} object located at the coordinates, or null if there is none
     */
    @Nullable Solid get(int x, int y, int z);
}
