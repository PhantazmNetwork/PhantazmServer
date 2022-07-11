package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

class BasicKeyedFactoryRegistry implements KeyedFactoryRegistry {
    private final Map<Key, KeyedFactory<?>> factoryMap;

    BasicKeyedFactoryRegistry() {
        this.factoryMap = new HashMap<>();
    }

    @Override
    public <TData> KeyedFactory<TData> getFactory(@NotNull Key type) {
        //noinspection unchecked
        return (KeyedFactory<TData>) factoryMap.get(type);
    }

    @Override
    public void registerFactory(@NotNull KeyedFactory<?> factory) {
        if(factoryMap.putIfAbsent(factory.key(), factory) != null) {
            throw new IllegalArgumentException("A factory for that key already has been registered");
        }
    }

    @Override
    public boolean hasFactory(@NotNull Key type) {
        return factoryMap.containsKey(type);
    }
}
