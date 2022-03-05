package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.Nullable;

public interface TerrainCollider {
    @Nullable Vec3I snap(int x, int y, int z, int moveX, int moveY, int moveZ);
}
