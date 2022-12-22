package org.phantazm.neuron.world;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.pipe.Pipe;

/**
 * An extension of {@link Pipe.Source} specializing in iterating {@link Solid}s.
 */
@FunctionalInterface
public interface SolidSource extends Pipe.Source<Solid> {
    @Override
    @NotNull SolidPipe iterator();
}
