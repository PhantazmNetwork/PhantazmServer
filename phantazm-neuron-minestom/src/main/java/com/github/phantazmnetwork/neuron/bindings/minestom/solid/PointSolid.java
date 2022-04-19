package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.IteratorUtils;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

class PointSolid extends MinestomSolid {
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
