package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an append-only registry of {@link KeyedFactory} instances. Generally, this class should not be used
 * directly, but instead by a {@link ComponentBuilder} implementation that ensures some limited amount of type safety.
 */
public interface KeyedFactoryRegistry {
    /**
     * Retrieves a {@link KeyedFactory} instance given its associated key.
     *
     * @param type         the factory's key
     * @param <TData>      the type of data the factory accepts
     * @param <TComponent> the type of component the factory creates
     * @return the factory associated with the key, or null if none exists
     */
    <TData extends Keyed, TComponent> KeyedFactory<TData, TComponent> getFactory(@NotNull Key type);

    /**
     * Registers a factory. If a factory under the provided key already exists, an exception is thrown.
     *
     * @param key     the key used to register the factory
     * @param factory the factory to register
     */
    void registerFactory(@NotNull Key key, @NotNull KeyedFactory<?, ?> factory);

    /**
     * Determines if this registry has a factory registered under the provided key.
     *
     * @param type the key to check
     * @return true if a factory exists under the provided key, false otherwise
     */
    boolean hasFactory(@NotNull Key type);
}
