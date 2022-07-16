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

        @Override
        public boolean load(@NotNull Iterable<? extends Key> dependencies) {
            return !dependencies.iterator().hasNext();
        }

        @Override
        public boolean hasLoaded(@NotNull Key key) {
            return false;
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
            return new LazyDependencyProvider(key -> null);
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
     * Attempts to provide the following named dependency. If the dependency is not prepared, an exception will be
     * thrown.
     *
     * @param key           the identifier of the dependency
     * @param <TDependency> the runtime type of the dependency
     * @return the dependency
     */
    <TDependency> TDependency provide(@NotNull Key key);

    /**
     * Prepares all the given dependencies. If a dependency cannot be resolved, this method will return false.
     *
     * @param dependencies the dependencies to resolve
     * @return true if all dependencies were prepared successfully, false if there is a missing dependency
     */
    boolean load(@NotNull Iterable<? extends Key> dependencies);

    /**
     * Determines if this DependencyProvider has a dependency with the given key name.
     *
     * @param key the dependency to check for
     * @return true if the dependency exists and is loaded, false otherwise
     */
    boolean hasLoaded(@NotNull Key key);
}
