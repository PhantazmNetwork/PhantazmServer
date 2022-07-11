package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class LazyDependencyProvider implements DependencyProvider {
    private final Function<Key, Object> dependencyFunction;

    private final Map<Key, Object> loadedDependencies;

    LazyDependencyProvider(@NotNull Function<Key, Object> dependencyFunction, int size) {
        this.dependencyFunction = Objects.requireNonNull(dependencyFunction, "dependencyFunction");
        this.loadedDependencies = new HashMap<>(size);
    }

    @Override
    public <TDependency> TDependency provide(@NotNull Key key) {
        if(loadedDependencies.containsKey(key)) {
            //noinspection unchecked
            return (TDependency) loadedDependencies.get(key);
        }

        throw new IllegalArgumentException("No loaded dependency available for key " + key);
    }

    @Override
    public boolean prepare(@NotNull Iterable<? extends Key> dependencies) {
        for(Key key : dependencies) {
            Object dependency = dependencyFunction.apply(key);
            if(dependency == null) {
                return false;
            }

            loadedDependencies.put(key, dependency);
        }

        return true;
    }
}
