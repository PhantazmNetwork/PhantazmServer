package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.Nullable;

/**
 * Performs collision checks. WIP, will likely undergo significant changes, and so is lacking much javadoc.
 */
public interface Collider {
    @Nullable Vec3I snap(int x, int y, int z, int moveX, int moveY, int moveZ, float jump, float fall);
}
