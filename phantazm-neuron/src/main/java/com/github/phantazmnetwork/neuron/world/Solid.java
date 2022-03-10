package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;

public interface Solid extends Vec3I {
    float minX();

    float minY();

    float minZ();

    float maxX();

    float maxY();

    float maxZ();
}
