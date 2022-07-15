package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a basic {@link ConfigProcessor} registry which allows the serialization and deserialization of arbitrary
 * data based on {@link Key}. Generally, this class should not be used directly, but instead by a
 * {@link ComponentBuilder} implementation that ensures some limited amount of type safety.
 */
public interface KeyedConfigRegistry {
    /**
     * Registers the provided processor under the specified key. If a processor is already registered, an exception
     * will be thrown.
     *
     * @param key       the key uniquely identifying this processor
     * @param processor the processor to associate with the key
     */
    void registerProcessor(@NotNull Key key, @NotNull KeyedConfigProcessor<? extends Keyed> processor);

    /**
     * Determines if this registry has a processor registered under the given key.
     *
     * @param type the processor key to check for
     * @return true if a processor has been registered under this key, false otherwise
     */
    boolean hasProcessor(@NotNull Key type);

    /**
     * Attempts to deserialize the provided {@link ConfigNode} to a {@link Keyed} object. The node is expected to
     * provide a properly-formatted {@link Key} string under the name {@link KeyedConfigProcessor#SERIAL_KEY_NAME}.
     *
     * @param node the node to deserialize
     * @return the deserialized data
     * @throws ConfigProcessException if an error occurred during config processing
     */
    @NotNull Keyed deserialize(@NotNull ConfigNode node) throws ConfigProcessException;

    /**
     * Serializes the provided data to a generic ConfigNode.
     *
     * @param data the data to serialize
     * @return the serialized data
     * @throws ConfigProcessException if an error occurred during serialization
     */
    @NotNull ConfigNode serialize(@NotNull Keyed data) throws ConfigProcessException;

    @NotNull Key extractKey(@NotNull ConfigNode data) throws ConfigProcessException;
}
