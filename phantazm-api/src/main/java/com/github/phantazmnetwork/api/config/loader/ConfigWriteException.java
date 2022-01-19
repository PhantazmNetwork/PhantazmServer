package com.github.phantazmnetwork.api.config.loader;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that writing config has failed
 */
public class ConfigWriteException extends Exception {

    public ConfigWriteException(@NotNull Throwable cause) {
        super(cause);
    }

}
