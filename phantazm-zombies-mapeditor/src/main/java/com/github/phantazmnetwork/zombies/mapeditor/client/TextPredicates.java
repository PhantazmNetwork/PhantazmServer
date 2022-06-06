package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class TextPredicates {
    private TextPredicates() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Predicate<String> validKeyPredicate() {
        return string -> {
            try {
                //noinspection PatternValidation
                Key.key(Namespaces.PHANTAZM, string);
                return true;
            }
            catch (InvalidKeyException ignored) {
                return false;
            }
        };
    }
}
