package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link Solid} implementation based on a combination of exactly two other solids.
 */
class CompositeSolid extends PointSolid {
    private interface FloatToDoubleBiFunction {
        double apply(float x, float y);
    }

    private final Iterable<Solid> children;

    CompositeSolid(@NotNull Solid first, @NotNull Solid second) {
        super(computePoint(first.getMin(), second.getMin(), Math::min), computePoint(first.getMax(), second.getMax(),
                Math::max));
        this.children = List.of(first, second);
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public @NotNull Iterable<Solid> getChildren() {
        return children;
    }

    private static Vec3F computePoint(Vec3F first, Vec3F second, FloatToDoubleBiFunction selector) {
        return Vec3F.ofDouble(selector.apply(first.getX(), second.getX()), selector.apply(first.getY(), second.getY()),
                selector.apply(first.getZ(), second.getZ()));
    }
}
