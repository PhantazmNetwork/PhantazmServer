package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Controller {
    double getX();

    double getY();

    double getZ();

    void moveTo(@NotNull Vec3I vec3I);
}
