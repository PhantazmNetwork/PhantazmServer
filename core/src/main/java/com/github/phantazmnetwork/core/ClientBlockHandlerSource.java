package com.github.phantazmnetwork.core;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Provides {@link ClientBlockHandler} instances corresponding to Minestom {@link Instance} objects.
 */
@FunctionalInterface
public interface ClientBlockHandlerSource {
    /**
     * Creates or returns an existing {@link ClientBlockHandler} instance for the provided Minestom {@link Instance}.
     *
     * @param instance the instance for which to create or retrieve a ClientBlockHandler
     * @return the ClientBlockHandler for the given instance
     */
    @NotNull ClientBlockHandler forInstance(@NotNull Instance instance);
}
