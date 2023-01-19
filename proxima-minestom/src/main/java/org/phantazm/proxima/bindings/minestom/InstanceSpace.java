package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.solid.Solid;
import com.github.steanky.proxima.space.ConcurrentCachingSpace;
import com.github.steanky.vector.Bounds3D;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceSpace extends ConcurrentCachingSpace {
    private static final Map<Shape, Solid> shapeMap;
    private static final Map<Shape, Solid[]> splitMap;

    static {
        shapeMap = new ConcurrentHashMap<>();
        splitMap = new ConcurrentHashMap<>();
    }

    private final Instance instance;

    public InstanceSpace(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public @Nullable Solid loadSolid(int x, int y, int z) {
        Chunk chunk = instance.getChunk(x, z);
        if (chunk == null) {
            return null;
        }

        instance.getBlock(x, y, z);

        Block block = getBlock(chunk, x, y, z);
        if (block == null) {
            return null;
        }

        Shape shape = block.registry().collisionShape();
        if (shape.isFullBlock()) {
            return Solid.FULL;
        }

        double endY = shape.relativeEnd().y();
        if (endY > 1) {
            //tall block, grab the lower split
            Solid solid = getSplit(shape)[0];

            Point start = shape.relativeStart();
            Point end = shape.relativeEnd();

            if (start.x() == 0 && start.z() == 0 && end.x() == 1 && end.z() == 1) {
                return solid;
            }

            Block below = getBlock(chunk, x, y - 1, z);
            if (below == null) {
                return solid;
            }

            Shape belowShape = below.registry().collisionShape();
            if (belowShape.isEmpty() || belowShape.isFullBlock() || belowShape.relativeEnd().y() <= 1) {
                return solid;
            }

            Solid mergeTarget = getSplit(belowShape)[1];
            List<Bounds3D> ourChildren = solid.children();
            List<Bounds3D> mergeTargetChildren = mergeTarget.children();

            Bounds3D[] mergedBounds = new Bounds3D[mergeTargetChildren.size() + ourChildren.size()];

            int i = 0;
            for (Bounds3D child : ourChildren) {
                mergedBounds[i++] = child;
            }

            for (Bounds3D child : mergeTargetChildren) {
                mergedBounds[i++] = child;
            }

            return Solid.of(mergedBounds);
        }

        if (endY >= 0.5) {
            //no tall blocks are more than 1.5 tall, so don't check below
            return cachedSolid(shape);
        }

        Block below = getBlock(chunk, x, y - 1, z);
        if (below == null) {
            return cachedSolid(shape);
        }

        Shape belowShape = below.registry().collisionShape();
        double belowEndY = belowShape.relativeEnd().y();
        if (belowEndY > 1) {
            Solid target = getSplit(belowShape)[1];
            if (shape.isEmpty()) {
                return target;
            }

            List<Bounds3D> extraChildren = target.children();
            List<BoundingBox> ourChildren = shape.childBounds();
            Bounds3D[] newBoundsArray = new Bounds3D[extraChildren.size() + ourChildren.size()];

            int i = 0;
            for (Bounds3D extraChild : extraChildren) {
                newBoundsArray[i++] = extraChild;
            }

            for (BoundingBox boundingBox : ourChildren) {
                newBoundsArray[i++] = Bounds3D.immutable(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                        boundingBox.width(), boundingBox.height(), boundingBox.depth());
            }

            return Solid.of(newBoundsArray);
        }

        return cachedSolid(shape);
    }

    public @NotNull Instance instance() {
        return instance;
    }

    private static Block getBlock(Chunk chunk, int x, int y, int z) {
        synchronized (chunk) {
            return chunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
        }
    }

    private static Solid[] getSplit(Shape shape) {
        return splitMap.computeIfAbsent(shape, (s) -> {
            List<BoundingBox> bounds = shape.childBounds();

            List<Bounds3D> lower = new ArrayList<>(bounds.size());
            List<Bounds3D> upper = new ArrayList<>(bounds.size());

            for (BoundingBox boundingBox : bounds) {
                if (boundingBox.maxY() > 1) {
                    double minY = boundingBox.minY();
                    upper.add(Bounds3D.immutable(boundingBox.minX(), 1, boundingBox.minZ(), boundingBox.width(),
                            boundingBox.height() - 1, boundingBox.depth()));

                    if (minY < 1) {
                        lower.add(Bounds3D.immutable(boundingBox.minX(), minY, boundingBox.minZ(), boundingBox.width(),
                                1 - minY, boundingBox.depth()));
                    }
                }
                else {
                    lower.add(Bounds3D.immutable(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                            boundingBox.width(), boundingBox.height(), boundingBox.depth()));
                }
            }

            return new Solid[] {Solid.of(lower.toArray(Bounds3D[]::new)), Solid.of(upper.toArray(Bounds3D[]::new))};
        });
    }

    private static Solid cachedSolid(Shape shape) {
        return shapeMap.computeIfAbsent(shape, key -> {
            List<BoundingBox> boundingBoxes = key.childBounds();
            Bounds3D[] bounds = new Bounds3D[boundingBoxes.size()];

            for (int i = 0; i < bounds.length; i++) {
                BoundingBox box = boundingBoxes.get(i);
                bounds[i] =
                        Bounds3D.immutable(box.minX(), box.minY(), box.minZ(), box.width(), box.height(), box.depth());
            }

            return Solid.of(bounds);
        });
    }
}
