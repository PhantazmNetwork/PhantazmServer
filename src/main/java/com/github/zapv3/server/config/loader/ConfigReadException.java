package com.github.zapv3.server.config.loader;

import org.jetbrains.annotations.NotNull;

public class ConfigReadException extends Exception {

    public ConfigReadException(@NotNull String message) {
        super(message);
    }

    public ConfigReadException(@NotNull Throwable cause) {
        super(cause);
    }

}
