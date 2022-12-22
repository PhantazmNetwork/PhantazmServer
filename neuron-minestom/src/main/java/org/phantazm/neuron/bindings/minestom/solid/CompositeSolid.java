package org.phantazm.neuron.bindings.minestom.solid;

import com.github.steanky.vector.Vec3D;
import org.jetbrains.annotations.NotNull;
import org.phantazm.neuron.world.Solid;

import java.util.Set;

/**
 * A {@link Solid} implementation based on a combination of exactly two other solids.
 */
class CompositeSolid extends PointSolid {
    private final Set<Solid> children;

    /**
     * Creates a new CompositeSolid from two other solids. The solids must not be equal, or an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param first  the first solid
     * @param second the second solid
     */
    CompositeSolid(@NotNull Solid first, @NotNull Solid second) {
        super(computePoint(first.getMin(), second.getMin(), Math::min),
                computePoint(first.getMax(), second.getMax(), Math::max));
        this.children = Set.of(first, second);
    }

    private static Vec3D computePoint(Vec3D first, Vec3D second, Double2DoubleBiFunction selector) {
        return Vec3D.immutable(selector.apply(first.x(), second.x()), selector.apply(first.y(), second.y()),
                selector.apply(first.z(), second.z()));
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public @NotNull Set<Solid> getChildren() {
        return children;
    }

    private interface Double2DoubleBiFunction {
        double apply(double x, double y);
    }
}
