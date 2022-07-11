package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BasicKeyedFactoryRegistry implements KeyedFactoryRegistry {
    private final Map<Key, KeyedFactory<?, ?>> factoryMap;

    public BasicKeyedFactoryRegistry() {
        this.factoryMap = new HashMap<>();
    }

    @Override
    public <TData extends Keyed, TComponent> KeyedFactory<TData, TComponent> getFactory(@NotNull Key type) {
        //noinspection unchecked
        return (KeyedFactory<TData, TComponent>) factoryMap.get(type);
    }

    @Override
    public void registerFactory(@NotNull Key key, @NotNull KeyedFactory<?, ?> factory) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(factory, "factory");

        if(factoryMap.putIfAbsent(key, factory) != null) {
            throw new IllegalArgumentException("A factory for that key already has been registered");
        }
    }

    @Override
    public boolean hasFactory(@NotNull Key type) {
        return factoryMap.containsKey(type);
    }
}
