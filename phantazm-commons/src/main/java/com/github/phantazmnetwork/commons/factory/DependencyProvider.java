package com.github.phantazmnetwork.commons.factory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Provides dependencies based on their associated {@link Key}s.
 */
public interface DependencyProvider {

    /**
     * Gets an individual dependency.
     * @param key The {@link Key} of the dependency
     * @return The dependency associated with the {@code key}
     * @param <T> The type of the dependency
     */
    <T> @NotNull T getDependency(@NotNull Key key);

    /**
     * Provides a {@link Collection} of dependencies.
     * @param keys The keys of the dependencies
     * @return A {@link Collection} of dependencies
     * @param <T> The shared data type of the dependencies
     */
    default <T> @NotNull Collection<T> getDependencies(@NotNull Collection<Key> keys) {
        Collection<T> dependencies = new ArrayList<>(keys.size());
        for (Key key : keys) {
            dependencies.add(getDependency(key));
        }

        return dependencies;
    }

    /**
     * Checks whether the provider has a dependency.
     * @param key The {@link Key} of the dependency
     * @return Whether the provider has the dependency associated with the {@code key}
     */
    boolean hasDependency(@NotNull Key key);

}
