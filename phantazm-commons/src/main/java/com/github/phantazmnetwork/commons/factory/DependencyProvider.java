package com.github.phantazmnetwork.commons.factory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@FunctionalInterface
public interface DependencyProvider {

    <T> @NotNull T getDependency(@NotNull Key key);

    default <T> @NotNull Collection<T> getDependency(@NotNull Collection<Key> keys) {
        Collection<T> dependencies = new ArrayList<>(keys.size());
        for (Key key : keys) {
            dependencies.add(getDependency(key));
        }

        return dependencies;
    }

}
