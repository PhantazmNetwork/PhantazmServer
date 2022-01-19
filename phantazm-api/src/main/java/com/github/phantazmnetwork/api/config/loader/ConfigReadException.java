package com.github.phantazmnetwork.api.config.loader;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that reading config has failed.
 */
public class ConfigReadException extends Exception {

    /**
     * Creates a {@link ConfigReadException} with a supplied message.
     * @param message The message for this exception
     */
    public ConfigReadException(@NotNull String message) {
        super(message);
    }

    /**
     * Creates a {@link ConfigReadException} with a defined cause.
     * @param cause The cause for this exception
     */
    public ConfigReadException(@NotNull Throwable cause) {
        super(cause);
    }

}
