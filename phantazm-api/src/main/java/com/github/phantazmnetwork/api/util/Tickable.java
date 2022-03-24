package com.github.phantazmnetwork.api.util;

/**
 * An interfaces which allows implementations to have periodic behavior.
 */
@FunctionalInterface
public interface Tickable {

    /**
     * Executes a "tick".
     * This enables implementations to run periodic behavior.
     */
    void tick();

}
