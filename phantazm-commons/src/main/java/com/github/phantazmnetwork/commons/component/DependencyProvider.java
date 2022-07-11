package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface DependencyProvider {
    <TDependency> TDependency provide(@NotNull Key key);

    boolean prepare(@NotNull Iterable<? extends Key> dependencies);

    static @NotNull DependencyProvider lazy(@NotNull Function<Key, Object> dependencyFunction) {
        return new LazyDependencyProvider(dependencyFunction, 5);
    }
}