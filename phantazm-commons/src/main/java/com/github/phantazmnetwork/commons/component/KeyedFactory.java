package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface KeyedFactory<TData> extends Keyed {
    @NotNull TData make(@NotNull DependencyProvider dependencyProvider, @NotNull Keyed data);

    @NotNull Collection<Key> dependencySpec();
}
