package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

class ComponentDependencyProvider extends LazyDependencyProvider {
    /**
     * Creates a new instance of this class using the provided dependency creation function.
     *
     * @param dependencyFunction the dependency creation function
     */
    ComponentDependencyProvider(@NotNull Function<? super Key, ?> dependencyFunction) {
        super(dependencyFunction);
    }
}
