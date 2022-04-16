package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.IteratorUtils;
import com.github.phantazmnetwork.commons.minestom.vector.VecUtils;
import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Shape}-based implementation of {@link Solid}. This is not part of the public API.
 */
@SuppressWarnings("UnstableApiUsage")
class MinestomSolid implements Solid {
    private final Vec3F min;
    private final Vec3F max;
    private final boolean hasChildren;
    private final Iterable<Solid> children;

    MinestomSolid(@NotNull Shape shape) {
        this.min = VecUtils.toFloat(shape.relativeStart());
        this.max = VecUtils.toFloat(shape.relativeEnd());

        List<BoundingBox> children = shape.boundingBoxes();
        if(children.size() == 0) {
            this.hasChildren = false;
            this.children = Pipe::empty;
        }
        else {
            ArrayList<Solid> childSolids = new ArrayList<>();
            for(BoundingBox boundingBox : children) {
                //recursive constructor is safe (on latest minestom build)
                //children.size() will be empty for all child bounding boxes
                childSolids.add(new MinestomSolid(boundingBox));
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
    public @NotNull Vec3F getMin() {
        return min;
    }

    @Override
    public @NotNull Vec3F getMax() {
        return max;
    }

    @Override
    public @NotNull Iterable<Solid> getChildren() {
        return children;
    }

    @Override
    public boolean overlaps(double x, double y, double z, double width, double height, double depth) {
        return x < max.getX() && y < max.getY() && z < max.getZ() && x + width > min.getX() && y + height > min.getY()
                && z + depth > min.getZ();
    }
}
