package com.github.phantazmnetwork.phantazmserver.server.config.loader;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that reading config has failed
 */
public class ConfigReadException extends Exception {

    public ConfigReadException(@NotNull String message) {
        super(message);
    }

    public ConfigReadException(@NotNull Throwable cause) {
        super(cause);
    }

}
