package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface PathContext {
    @Nullable Iterable<Vec3I> getStep(int descriptor, @NotNull Vec3I origin);

    @NotNull Iterator<Vec3I> watchSteps(int descriptor, @NotNull Vec3I origin,
                                        @NotNull Iterator<? extends Vec3I> steps);

    void invalidateOrigin(@NotNull Vec3I origin);

    void invalidateOrigins(@NotNull Iterable<? extends Vec3I> steps);

    void invalidateAll();
}
