package com.github.phantazmnetwork.api.config.loader;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that writing config has failed.
 */
public class ConfigWriteException extends Exception {

    /**
     * Creates a {@link ConfigWriteException} with a defined cause.
     * @param cause The cause for this exception
     */
    public ConfigWriteException(@NotNull Throwable cause) {
        super(cause);
    }

}
