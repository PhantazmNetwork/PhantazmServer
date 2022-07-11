package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface KeyedFactoryRegistry {
    <TComponent, TData extends Keyed> KeyedFactory<TData, TComponent> getFactory(@NotNull Key type);

    void registerFactory(@NotNull KeyedFactory<?, ?> factory);

    boolean hasFactory(@NotNull Key type);
}
