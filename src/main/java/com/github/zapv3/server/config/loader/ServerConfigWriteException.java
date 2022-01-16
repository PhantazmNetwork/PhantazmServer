package com.github.zapv3.server.config.loader;

import org.jetbrains.annotations.NotNull;

public class ServerConfigWriteException extends Exception {

    public ServerConfigWriteException(@NotNull Throwable cause) {
        super(cause);
    }

}
