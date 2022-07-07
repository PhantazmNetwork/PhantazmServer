package com.github.phantazmnetwork.commons.factory;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FactoryDependencyProvider implements DependencyProvider {

    private final Map<Key, Keyed> loadedData;

    private final Map<Key, Object> loadedObjects;

    private final Map<Key, Factory<?, ?>> factories;

    public FactoryDependencyProvider(@NotNull Map<Key, Keyed> loadedData, @NotNull Map<Key, Factory<?, ?>> factories) {
        this.loadedData = Map.copyOf(Objects.requireNonNull(loadedData, "loadedData"));
        this.factories = Map.copyOf(Objects.requireNonNull(factories, "factories"));
        for (Key key : loadedData.keySet()) {
            if (!factories.containsKey(key)) {
                throw new IllegalArgumentException("No factory found for key " + key);
            }
        }
        this.loadedObjects = new HashMap<>(loadedData.size());
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull T getDependency(@NotNull Key key) {
        Objects.requireNonNull(key, "key");
        Object object = loadedObjects.get(key);
        if (object == null) { // computeIfAbsent results in a CME
            Keyed data = loadedData.get(key);
            if (data == null) {
                throw new IllegalArgumentException("No data found for key: " + key);
            }

            Factory<Keyed, ?> factory = (Factory<Keyed, ?>) factories.get(data.key());
            loadedObjects.put(key, Objects.requireNonNull(object = factory.make(this, data),
                    "factory produced null object"));
        }

        return (T) object;
    }

    @Override
    public boolean hasDependency(@NotNull Key key) {
        return loadedData.containsKey(key);
    }
}
