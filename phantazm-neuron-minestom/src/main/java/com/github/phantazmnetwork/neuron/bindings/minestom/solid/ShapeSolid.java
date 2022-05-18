package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.neuron.world.Solid;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link Shape}-based implementation of {@link Solid}. Not part of the public API.
 */
class ShapeSolid extends PointSolid {
    private final boolean hasChildren;
    private final Set<Solid> children;

    /**
     * Creates a new instance of this class from the provided {@link Shape}. The shape's child bounding boxes, if any,
     * will be accounted for.
     * @param shape the shape to create this solid from
     */
    ShapeSolid(@NotNull Shape shape) {
        super(VecUtils.toFloat(shape.relativeStart()), VecUtils.toFloat(shape.relativeEnd()));

        List<BoundingBox> children = shape.childBounds();
        if(children.isEmpty()) {
            this.hasChildren = false;
            this.children = Collections.emptySet();
        }
        else {
            List<Solid> childSolids = new ArrayList<>(children.size());
            for(BoundingBox boundingBox : children) {
                childSolids.add(new PointSolid(VecUtils.toFloat(boundingBox.relativeStart()), VecUtils.toFloat(
                        boundingBox.relativeEnd())));
            }

            this.hasChildren = true;
            this.children = Set.of(childSolids.toArray(new Solid[0]));
        }
    }

    @Override
    public boolean hasChildren() {
        return hasChildren;
    }


    @Override
    public @Unmodifiable @NotNull Set<Solid> getChildren() {
        return children;
    }
}
