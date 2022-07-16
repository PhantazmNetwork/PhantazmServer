package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.annotation.ComponentDependency;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a provider of dependencies (arbitrary data needed by a component that is not itself part of its
 * configuration).
 */
public interface DependencyProvider {
    /**
     * The shared, always-empty DependencyProvider implementation.
     */
    DependencyProvider EMPTY = new DependencyProvider() {
        @Override
        public <TDependency> TDependency provide(@NotNull Key key) {
            throw new IllegalArgumentException();
        }
    };

    /**
     * Creates a new {@link DependencyProvider} implementation that will lazily resolve dependencies using the given
     * function during the "prepare" phase.
     *
     * @param dependencyFunction the function used to create dependencies
     * @return a new DependencyProvider implementation
     */
    static @NotNull DependencyProvider lazy(@NotNull Function<? super Key, ?> dependencyFunction) {
        return new LazyDependencyProvider(dependencyFunction);
    }

    /**
     * Creates a new {@link DependencyProvider} implementation capable of providing the objects in the given array as
     * dependencies. Each object must either implement {@link Keyed}, or supply the ComponentDependency annotation with
     * an appropriately formatted key string. If multiple dependencies have the same key, only the first object will be
     * used.
     *
     * @param objects the objects to use as dependencies
     * @return a new DependencyProvider that can provide the given objects as dependencies
     */
    static @NotNull DependencyProvider ofDependencies(Object... objects) {
        if (objects == null || objects.length == 0) {
            return EMPTY;
        }

        Map<Key, Object> mappings = new HashMap<>(objects.length);
        for (Object object : objects) {
            if (object instanceof Keyed keyed) {
                mappings.putIfAbsent(keyed.key(), object);
                continue;
            }

            ComponentDependency dependencyAnnotation = object.getClass().getAnnotation(ComponentDependency.class);
            if (dependencyAnnotation == null) {
                throw new IllegalArgumentException("Dependency " + object + " does not implement Keyed or provide a " +
                                                   "ComponentDependency annotation");
            }

            @Subst(Namespaces.PHANTAZM + ":test")
            String value = dependencyAnnotation.value();

            Key key = Key.key(value);
            mappings.putIfAbsent(key, object);
        }

        return new LazyDependencyProvider(mappings::get);
    }

    /**
     * Attempts to provide the following named dependency.
     *
     * @param key           the identifier of the dependency
     * @param <TDependency> the runtime type of the dependency
     * @return the dependency
     */
    <TDependency> TDependency provide(@NotNull Key key);
}
