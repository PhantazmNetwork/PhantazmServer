package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class SolidProvider {
    private static final Map<Shape, Solid> SHARED_SOLIDS = new Object2ObjectOpenCustomHashMap<>(512,
            HashStrategies.identity());

    private SolidProvider() { throw new UnsupportedOperationException(); }

    public static @NotNull Solid fromShape(@NotNull Shape shape) {
        return SHARED_SOLIDS.computeIfAbsent(shape, MinestomSolid::new);
    }
}
