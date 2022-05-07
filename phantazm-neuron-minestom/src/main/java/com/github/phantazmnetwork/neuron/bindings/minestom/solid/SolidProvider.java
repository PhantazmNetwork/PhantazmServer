package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Provides {@link Solid} implementations for Minestom.
 * @see MinestomSolid
 * @see ShapeSolid
 * @see PointSolid
 */
public final class SolidProvider {
    private static final Map<Shape, Solid> SHAPE_SOLIDS = new Object2ObjectOpenCustomHashMap<>(HashStrategies
            .identity());

    private SolidProvider() { throw new UnsupportedOperationException(); }

    /**
     * Returns a {@link Solid} implementation from the specified {@link Shape}. Values may or may not be returned from a
     * shared cache.
     * @param shape the shape to create a solid from
     * @return a Solid implementation, possibly from cache, representing the given shape
     */
    public static @NotNull Solid fromShape(@NotNull Shape shape) {
        Objects.requireNonNull(shape, "shape");
        Solid solid;
        synchronized (SHAPE_SOLIDS) {
            solid = SHAPE_SOLIDS.get(shape);
        }

        if(solid != null) {
            return solid;
        }

        Solid newSolid = new ShapeSolid(shape);
        synchronized (SHAPE_SOLIDS) {
            SHAPE_SOLIDS.put(shape, newSolid);
        }

        return newSolid;
    }

    /**
     * Creates a new {@link Solid} implementation from the given {@code min} and {@code max} points.
     * @param min the min point
     * @param max the max point
     * @return a new Solid implementation from the given points
     */
    public static @NotNull Solid fromPoints(@NotNull Vec3F min, @NotNull Vec3F max) {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        return new PointSolid(min, max);
    }
}
