package com.github.phantazmnetwork.commons.factory;

import org.jetbrains.annotations.NotNull;

/**
 * Creates a specific type of object.
 *
 * @param <TIn>  The input data type of the factory
 * @param <TOut> The output data type of the factory
 */
@FunctionalInterface
public interface Factory<TIn, TOut> {

    /**
     * Creates a specific type of object.
     *
     * @param provider A {@link DependencyProvider} used to provide dependencies to create the object
     * @param data     The input data used to create the object
     * @return The new output object
     */
    @NotNull TOut make(@NotNull DependencyProvider provider, @NotNull TIn data);
}
