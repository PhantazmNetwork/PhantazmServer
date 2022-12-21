package org.phantazm.neuron.bindings.minestom.solid;

import com.github.steanky.vector.Vec3D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.neuron.world.Solid;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class PointSolid implements Solid {
    private final Vec3D min;
    private final Vec3D max;

    /**
     * Creates a new instance of this class from the given min and max vectors.
     *
     * @param min the min vector
     * @param max the max vector
     */
    PointSolid(@NotNull Vec3D min, @NotNull Vec3D max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public @NotNull Vec3D getMin() {
        return min;
    }

    @Override
    public @NotNull Vec3D getMax() {
        return max;
    }

    @Override
    public @Unmodifiable @NotNull Set<Solid> getChildren() {
        return Collections.emptySet();
    }

    @Override
    public boolean overlaps(double x, double y, double z, double width, double height, double depth) {
        boolean overlapsSelf =
                min.x() < x + width && max.x() > x && min.y() < y + height && max.y() > y && min.z() < z + depth &&
                        max.z() > z;

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
