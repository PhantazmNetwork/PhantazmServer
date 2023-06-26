package org.phantazm.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.IllegalFormatException;
import java.util.Objects;

public final class ComponentUtils {
    private ComponentUtils() {
    }

    public static @NotNull Component tryFormat(@NotNull String formatString, Object... objects) {
        Objects.requireNonNull(formatString, "formatString");

        String message;
        try {
            message = String.format(formatString, objects);
        }
        catch (IllegalFormatException ignored) {
            message = formatString;
        }

        return MiniMessage.miniMessage().deserialize(message);
    }
}
