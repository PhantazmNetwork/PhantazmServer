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

@SuppressWarnings("UnstableApiUsage")
public class SolidProvider {
    private static final Map<Shape, Solid> SHAPE_SOLIDS = Object2ObjectMaps.synchronize(
            new Object2ObjectOpenCustomHashMap<>(HashStrategies.identity()));

    private SolidProvider() { throw new UnsupportedOperationException(); }

    public static @NotNull Solid fromShape(@NotNull Shape shape) {
        Objects.requireNonNull(shape, "shape");
        return SHAPE_SOLIDS.computeIfAbsent(shape, ShapeSolid::new);
    }

    public static @NotNull Solid fromPoints(@NotNull Vec3F min, @NotNull Vec3F max) {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        return new PointSolid(min, max);
    }
}
