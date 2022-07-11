package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

public interface KeyedFactory<TData extends Keyed, TComponent> extends Keyed {
    @NotNull TComponent make(@NotNull DependencyProvider dependencyProvider, @NotNull TData data);

    @Unmodifiable @NotNull List<Key> dependencySpec();
}
