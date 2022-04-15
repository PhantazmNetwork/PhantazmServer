package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

public interface Controller {
    double getX();

    double getY();

    double getZ();

    double getVelocityX();

    double getVelocityY();

    double getVelocityZ();

    void setVelocity(@NotNull Vec3D velocity);

    void init(PathResult result);

    @NotNull Vec3D advance(@NotNull Vec3I vec3I);
}
