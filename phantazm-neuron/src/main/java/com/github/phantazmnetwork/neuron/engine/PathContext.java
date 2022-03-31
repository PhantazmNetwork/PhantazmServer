package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

public interface PathContext {
    @NotNull Optional<Iterable<Vec3I>> getStep(@NotNull Vec3I origin, @NotNull Agent.Descriptor descriptor);

    @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull Agent.Descriptor descriptor,
                                        @NotNull Iterator<? extends Vec3I> steps);

    void invalidateOrigin(@NotNull Vec3I origin);

    void invalidateOrigins(@NotNull Iterable<? extends Vec3I> steps);

    void invalidateAll();
}
