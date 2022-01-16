package com.github.zapv3.server.config.loader;

import org.jetbrains.annotations.NotNull;

public class ConfigWriteException extends Exception {

    public ConfigWriteException(@NotNull Throwable cause) {
        super(cause);
    }

}
