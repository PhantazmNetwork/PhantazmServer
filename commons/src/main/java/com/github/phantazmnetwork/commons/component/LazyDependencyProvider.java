package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Lazily-initializing implementation of {@link DependencyProvider}.
 */
class LazyDependencyProvider implements DependencyProvider {
    /**
     * The dependency function used to instantiate new dependencies.
     */
    protected final Function<? super Key, ?> dependencyFunction;

    /**
     * The map of currently loaded dependencies.
     */
    protected final Map<Key, Object> loadedDependencies;

    /**
     * Creates a new instance of this class using the provided dependency creation function.
     *
     * @param dependencyFunction the dependency creation function
     */
    LazyDependencyProvider(@NotNull Function<? super Key, ?> dependencyFunction) {
        this.dependencyFunction = Objects.requireNonNull(dependencyFunction, "dependencyFunction");
        this.loadedDependencies = new HashMap<>(8);
    }

    @Override
    public <TDependency> TDependency provide(@NotNull Key key) {
        if (loadedDependencies.containsKey(key)) {
            //noinspection unchecked
            return (TDependency)loadedDependencies.get(key);
        }

        throw new IllegalArgumentException("No dependency available for key " + key);
    }

    @Override
    public boolean load(@NotNull Iterable<? extends Key> dependencies) {
        for (Key key : dependencies) {
            Object dependency = dependencyFunction.apply(key);
            if (dependency == null) {
                return false;
            }

            loadedDependencies.putIfAbsent(key, dependency);
        }

        return true;
    }

    @Override
    public boolean hasLoaded(@NotNull Key key) {
        return loadedDependencies.containsKey(key);
    }
}