package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.engine.PathContext;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ContextProvider {
    @NotNull PathContext provideContext(@NotNull Instance instance);
}
