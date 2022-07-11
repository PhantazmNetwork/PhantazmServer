package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface KeyedFactory<TData extends Keyed, TComponent> {
    @NotNull TComponent make(@NotNull DependencyProvider dependencyProvider, @NotNull TData data);

    @Unmodifiable @NotNull List<Key> dependencies();
}
