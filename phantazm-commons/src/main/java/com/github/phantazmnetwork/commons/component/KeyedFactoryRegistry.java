package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface KeyedFactoryRegistry {
    <TData> KeyedFactory<TData> getFactory(@NotNull Key type);

    void registerFactory(@NotNull KeyedFactory<?> factory);

    boolean hasFactory(@NotNull Key type);
}
