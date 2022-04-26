package com.github.phantazmnetwork.neuron.bindings.minestom.solid;

import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public final class SolidProvider {
    private static final Map<Shape, Solid> SHAPE_SOLIDS = new Object2ObjectOpenCustomHashMap<>(HashStrategies
            .identity());

    private SolidProvider() { throw new UnsupportedOperationException(); }

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

    public static @NotNull Solid fromPoints(@NotNull Vec3F min, @NotNull Vec3F max) {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        return new PointSolid(min, max);
    }
}
