package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Provides {@link Solid} implementations for Minestom.
 *
 * @see ShapeSolid
 * @see PointSolid
 */
public final class SolidProvider {
    private static final Map<Shape, Solid> SHAPE_SOLIDS =
            new Object2ObjectOpenCustomHashMap<>(HashStrategies.identity());
    private static final Map<Shape, Solid[]> SPLIT_MAP =
            new Object2ObjectOpenCustomHashMap<>(HashStrategies.identity());


    private SolidProvider() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a {@link Solid} implementation from the specified {@link Shape}. Values may or may not be returned from a
     * shared cache.
     *
     * @param shape the shape to create a solid from
     * @return a Solid implementation, possibly from cache, representing the given shape
     */
    public static @NotNull Solid fromShape(@NotNull Shape shape) {
        Objects.requireNonNull(shape, "shape");
        Solid solid;
        synchronized (SHAPE_SOLIDS) {
            solid = SHAPE_SOLIDS.get(shape);
        }

        if (solid != null) {
            return solid;
        }

        Solid newSolid = new ShapeSolid(shape);
        synchronized (SHAPE_SOLIDS) {
            SHAPE_SOLIDS.put(shape, newSolid);
        }

        return newSolid;
    }

    /**
     * Creates a new composite solid from the given solids. The two solids may not be equal.
     *
     * @param first  the first solid
     * @param second the second solid
     * @return a solid consisting of both of the original solids
     * @throws IllegalArgumentException if the two solids are equal
     */
    public static @NotNull Solid composite(@NotNull Solid first, @NotNull Solid second) {
        return new CompositeSolid(first, second);
    }

    /**
     * Returns the "split" for a tall {@link Shape}. The first half of the split will be exactly 1 block in height,
     * whereas the remainder will generally be less than 1 in height.
     *
     * @param shape the tall shape to split
     * @return an array with exactly two elements, the first being the lower portion of the shape, the second being the
     * higher portion
     */
    public static @NotNull Solid @NotNull [] getSplit(@NotNull Shape shape) {
        if (shape.relativeEnd().y() < 1) {
            throw new IllegalArgumentException("Can't split a shape that isn't tall");
        }

        return getSplitFor(shape);
    }

    /**
     * Creates a new {@link Solid} implementation from the given {@code min} and {@code max} points.
     *
     * @param min the min point
     * @param max the max point
     * @return a new Solid implementation from the given points
     */
    public static @NotNull Solid fromPoints(@NotNull Vec3F min, @NotNull Vec3F max) {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        return new PointSolid(min, max);
    }

    private static Solid[] getSplitFor(Shape tallShape) {
        Solid[] split;
        synchronized (SPLIT_MAP) {
            split = SPLIT_MAP.get(tallShape);
        }

        if (split != null) {
            return split;
        }

        Point start = tallShape.relativeStart();
        Point end = tallShape.relativeEnd();
        Solid[] newSplit = new Solid[] {fromPoints(VecUtils.toFloat(start), Vec3F.ofDouble(end.x(), 1, end.z())),
                                        fromPoints(Vec3F.ofDouble(start.x(), 0, start.z()),
                                                   Vec3F.ofDouble(end.x(), end.y() - 1, end.z())
                                        )};

        synchronized (SPLIT_MAP) {
            SPLIT_MAP.put(tallShape, newSplit);
        }

        return newSplit;
    }
}
