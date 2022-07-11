package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface KeyedFactoryRegistry {
    <TData extends Keyed, TComponent> KeyedFactory<TData, TComponent> getFactory(@NotNull Key type);

    void registerFactory(@NotNull Key key, @NotNull KeyedFactory<?, ?> factory);

    boolean hasFactory(@NotNull Key type);
}
