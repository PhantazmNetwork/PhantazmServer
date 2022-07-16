package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A factory which directly creates components from a {@link DependencyProvider} and some data.
 *
 * @param <TData>      the type of data to accept
 * @param <TComponent> the component to return
 */
public interface KeyedFactory<TData extends Keyed, TComponent> {
    /**
     * Creates a component using a {@link DependencyProvider} to resolve dependencies and some data.
     *
     * @param dependencyProvider the DependencyProvider used to resolve dependencies
     * @param data               the configurable data used to create the component
     * @return the component itself
     */
    @NotNull TComponent make(@NotNull DependencyProvider dependencyProvider, TData data);

    /**
     * Computes all the dependencies needed by this factory.
     *
     * @return the dependencies needed by this factory
     */
    @Unmodifiable @NotNull List<Key> computeDependencies(@NotNull TData data);
}
