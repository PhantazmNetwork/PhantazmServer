package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.Nullable;

public interface Collider {
    @Nullable Vec3I snap(int x, int y, int z, int moveX, int moveY, int moveZ, float jump, float fall);

    boolean collidesMovingAlong(int x, int y, int z, int moveX, int moveY, int moveZ);

    boolean collidesAt(int x, int y, int z, double width, double height);
}
