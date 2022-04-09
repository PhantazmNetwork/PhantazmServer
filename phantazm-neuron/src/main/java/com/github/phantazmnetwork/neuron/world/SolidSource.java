package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@FunctionalInterface
public interface SolidSource extends Pipe.Source<Solid> {
    @Override
    @NotNull SolidPipe iterator();
}
