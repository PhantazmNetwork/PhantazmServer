package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.IteratorUtils;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

/**
 * A concrete {@link MinestomSolid} implementation that has no children and is based solely on the min and max points.
 * Not part of the public API. Instances can be obtained via {@link SolidProvider#fromPoints(Vec3F, Vec3F)}, and child
 * solids that are part of a {@link ShapeSolid} are generally instances of this class as well.
 */
class PointSolid extends MinestomSolid {
    /**
     * Creates a new instance of this class from the given min and max vectors.
     * @param min the min vector
     * @param max the max vector
     */
    PointSolid(@NotNull Vec3F min, @NotNull Vec3F max) {
        super(min, max);
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public @NotNull Iterable<Solid> getChildren() {
        return IteratorUtils::empty;
    }
}
