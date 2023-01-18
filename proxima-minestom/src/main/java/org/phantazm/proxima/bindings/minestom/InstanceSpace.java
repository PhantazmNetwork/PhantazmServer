package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.solid.Solid;
import com.github.steanky.proxima.space.ConcurrentCachingSpace;
import com.github.steanky.vector.Bounds3D;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            return getSplit(shape)[0];
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
            if (endY > 0) {

            }
        }

        return null;
    }

    private static Block getBlock(Chunk chunk, int x, int y, int z) {
        synchronized (chunk) {
            return chunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
        }
    }

    private static Solid @NotNull [] getSplit(@NotNull Shape shape) {
        return splitMap.computeIfAbsent(shape, (s) -> {
            Point start = s.relativeStart();
            Point end = s.relativeEnd();

            double lx = end.x() - start.x();
            double lz = end.z() - start.z();

            Solid[] split = new Solid[2];
            split[0] = Solid.of(Bounds3D.immutable(start.x(), start.y(), start.z(), lx, 1, lz));
            split[1] = Solid.of(Bounds3D.immutable(start.x(), 0, start.z(), lx, end.y() - 1, lz));

            return split;
        });
    }

    private static Solid cachedSolid(Shape shape) {
        return null;
    }
}
