package org.phantazm.neuron.bindings.minestom;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.neuron.engine.PathContext;

/**
 * Represents a basic supplier of {@link PathContext} instances.
 */
@FunctionalInterface
public interface ContextProvider {
    /**
     * Provides a {@link PathContext}, given an {@link Instance}.
     *
     * @param instance the instance to provide a context for
     * @return the PathContext associated with this instance
     */
    @NotNull PathContext provideContext(@NotNull Instance instance);
}
