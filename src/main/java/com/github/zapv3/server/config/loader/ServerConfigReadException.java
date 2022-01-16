package com.github.zapv3.server.config.loader;

import org.jetbrains.annotations.NotNull;

public class ServerConfigReadException extends Exception {

    public ServerConfigReadException(@NotNull String message) {
        super(message);
    }

    public ServerConfigReadException(@NotNull Throwable cause) {
        super(cause);
    }

}
