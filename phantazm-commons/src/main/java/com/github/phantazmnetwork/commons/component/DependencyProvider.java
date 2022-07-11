package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a provider of dependencies (arbitrary data needed by a component that is not itself part of its
 * configuration).
 */
public interface DependencyProvider {
    /**
     * Attempts to provide the following named dependency. If the dependency is not prepared, an exception will be
     * thrown.
     * @param key the identifier of the dependency
     * @return the dependency
     * @param <TDependency> the runtime type of the dependency
     */
    <TDependency> TDependency provide(@NotNull Key key);

    /**
     * Prepares all the given dependencies. If a dependency cannot be resolved, this method will return false.
     * @param dependencies the dependencies to resolve
     * @return true if all dependencies were prepared successfully, false if there is a missing dependency
     */
    boolean prepare(@NotNull Iterable<? extends Key> dependencies);

    /**
     * Creates a new {@link DependencyProvider} implementation that will lazily resolve dependencies using the given
     * function during the "prepare" phase.
     * @param dependencyFunction the function used to create dependencies
     * @return a new DependencyProvider implementation
     */
    static @NotNull DependencyProvider lazy(@NotNull Function<? super Key, ?> dependencyFunction) {
        return new LazyDependencyProvider(dependencyFunction);
    }
}