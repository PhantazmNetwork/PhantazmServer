package com.github.phantazmnetwork.commons.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Represents "complex data". This data is used to create "functional components".
 * In particular, this is a serialized form of an object with nested composition.
 * This is formed with a {@link Map} of {@link Key}s to {@link Keyed} objects.
 * The key of the {@code objects} {@link Map} should be the same as the respective value's {@link Keyed#key()}.
 * @param mainKey The main key of the complex data, which can be used to
 *                instantiate the functional component that is represented by the complex data
 * @param objects A {@link Map} of the objects that are necessary to instantiate the functional component
 */
public record ComplexData(@NotNull Key mainKey, @NotNull Map<Key, Keyed> objects) {

    /**
     * Creates a {@link ComplexData}.
     * @param mainKey The main key of the complex data, which can be used to
     *                instantiate the functional component that is represented by the complex data
     * @param objects A {@link Map} of the objects that are necessary to instantiate the functional component
     */
    public ComplexData {
        Objects.requireNonNull(mainKey, "mainKey");
        Objects.requireNonNull(objects, "objects");
    }

}
