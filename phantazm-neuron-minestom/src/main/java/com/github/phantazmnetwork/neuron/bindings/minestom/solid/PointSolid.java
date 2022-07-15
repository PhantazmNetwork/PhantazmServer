package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class PointSolid implements Solid {
    private final Vec3F min;
    private final Vec3F max;

    /**
     * Creates a new instance of this class from the given min and max vectors.
     *
     * @param min the min vector
     * @param max the max vector
     */
    PointSolid(@NotNull Vec3F min, @NotNull Vec3F max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public @Unmodifiable @NotNull Set<Solid> getChildren() {
        return Collections.emptySet();
    }

    @Override
    public boolean overlaps(double x, double y, double z, double width, double height, double depth) {
        boolean overlapsSelf = min.getX() < x + width && max.getX() > x && min.getY() < y + height && max.getY() > y &&
                               min.getZ() < z + depth && max.getZ() > z;

        if (!hasChildren()) {
            return overlapsSelf;
        }

        if (!overlapsSelf) {
            return false;
        }

        for (Solid solid : getChildren()) {
            if (solid.overlaps(x, y, z, width, height, depth)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull Vec3F getMin() {
        return min;
    }

    @Override
    public @NotNull Vec3F getMax() {
        return max;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(min, max, getChildren());
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof Solid solid) {
            return min.equals(solid.getMin()) && max.equals(solid.getMax()) &&
                   getChildren().equals(solid.getChildren());
        }

        return false;
    }
}
