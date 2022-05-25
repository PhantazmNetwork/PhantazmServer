package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link Pipe.Source} specializing in iterating {@link Solid}s.
 */
@FunctionalInterface
public interface SolidSource extends Pipe.Source<Solid> {
    @Override
    @NotNull SolidPipe iterator();
}
