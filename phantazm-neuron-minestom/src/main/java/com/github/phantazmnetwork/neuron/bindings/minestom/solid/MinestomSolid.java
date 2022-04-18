package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

abstract class MinestomSolid implements Solid {
    private final Vec3F min;
    private final Vec3F max;

    MinestomSolid(@NotNull Vec3F min, @NotNull Vec3F max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean overlaps(double x, double y, double z, double width, double height, double depth) {
        return x < max.getX() && y < max.getY() && z < max.getZ() && x + width > min.getX() && y + height > min.getY()
                && z + depth > min.getZ();
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
