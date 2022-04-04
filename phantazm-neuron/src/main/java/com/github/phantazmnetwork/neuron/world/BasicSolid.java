package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import org.jetbrains.annotations.NotNull;

public class BasicSolid implements Solid {
    private final Vec3F min;
    private final Vec3F max;

    public BasicSolid(float x, float y, float z, float x2, float y2, float z2) {
        this.min = Vec3F.of(Math.min(x, x2), Math.min(y, y2), Math.min(z, z2));
        this.max = Vec3F.of(Math.max(x, x2), Math.max(y, y2), Math.max(z, z2));
    }

    @Override
    public @NotNull Vec3F getMin() {
        return min;
    }

    @Override
    public @NotNull Vec3F getMax() {
        return max;
    }
}