package org.phantazm.zombies.mapeditor.client;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

import java.util.function.Predicate;

/**
 * Contains shared {@code Predicate<String> instances}, used for verifying text input.
 */
public final class TextPredicates {
    private static final Predicate<String> validKeyPredicate = Key::parseableValue;

    private TextPredicates() {
        throw new UnsupportedOperationException();
    }

    /**
     * Predicate used to test if a string may be parsed into a valid {@link Key}. The namespace is assumed to be
     * {@link Namespaces#PHANTAZM}, so the given string is the key's value.
     *
     * @return a predicate used to check if a given string is a valid key
     */
    public static @NotNull Predicate<String> validKeyPredicate() {
        return validKeyPredicate;
    }
}
