package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Contains shared {@code Predicate<String> instances}, used for verifying text input.
 */
public final class TextPredicates {
    private static final Predicate<String> validKeyPredicate = string -> {
        try {
            //TODO: when adventure 4.13 is released, use their supplied key validation methods
            //noinspection PatternValidation
            Key.key(Namespaces.PHANTAZM, string);
            return true;
        }
        catch (InvalidKeyException ignored) {
            return false;
        }
    };

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
