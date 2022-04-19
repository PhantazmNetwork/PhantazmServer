package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.IteratorUtils;
import com.github.phantazmnetwork.commons.minestom.vector.VecUtils;
import com.github.phantazmnetwork.neuron.world.Solid;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Shape}-based implementation of {@link Solid}. Not part of the public API.
 */
@SuppressWarnings("UnstableApiUsage")
class ShapeSolid extends MinestomSolid {
    private final boolean hasChildren;
    private final Iterable<Solid> children;

    ShapeSolid(@NotNull Shape shape) {
        super(VecUtils.toFloat(shape.relativeStart()), VecUtils.toFloat(shape.relativeEnd()));

        List<BoundingBox> children = shape.boundingBoxes();
        if(children.size() == 0) {
            this.hasChildren = false;
            this.children = IteratorUtils::empty;
        }
        else {
            ArrayList<Solid> childSolids = new ArrayList<>();
            for(BoundingBox boundingBox : children) {
                childSolids.add(new PointSolid(VecUtils.toFloat(boundingBox.relativeStart()), VecUtils
                        .toFloat(boundingBox.relativeEnd())));
            }

            childSolids.trimToSize();
            this.hasChildren = true;
            this.children = () -> IteratorUtils.unmodifiable(childSolids.iterator());
        }
    }

    @Override
    public boolean hasChildren() {
        return hasChildren;
    }

    @Override
    public @NotNull Iterable<Solid> getChildren() {
        return children;
    }
}
