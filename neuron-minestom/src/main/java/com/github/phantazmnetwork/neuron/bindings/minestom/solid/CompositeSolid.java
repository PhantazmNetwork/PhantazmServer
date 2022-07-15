package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

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
              computePoint(first.getMax(), second.getMax(), Math::max)
        );
        this.children = Set.of(first, second);
    }

    private static Vec3F computePoint(Vec3F first, Vec3F second, FloatToDoubleBiFunction selector) {
        return Vec3F.ofDouble(selector.apply(first.getX(), second.getX()), selector.apply(first.getY(), second.getY()),
                              selector.apply(first.getZ(), second.getZ())
        );
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public @NotNull Set<Solid> getChildren() {
        return children;
    }

    private interface FloatToDoubleBiFunction {
        double apply(float x, float y);
    }
}
