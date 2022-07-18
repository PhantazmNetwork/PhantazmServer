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
    private final Function<? super Key, ?> dependencyFunction;

    /**
     * The map of currently loaded dependencies.
     */
    private final Map<Key, Object> loadedDependencies;

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
        TDependency dependency;
        if (loadedDependencies.containsKey(key)) {
            //noinspection unchecked
            dependency = (TDependency)loadedDependencies.get(key);
        }
        else {
            Object newDependency = dependencyFunction.apply(key);
            if (newDependency == null) {
                throw new IllegalArgumentException("Failed to resolve dependency " + key);
            }

            //noinspection unchecked
            loadedDependencies.put(key, dependency = (TDependency)newDependency);
        }

        return dependency;
    }
}
