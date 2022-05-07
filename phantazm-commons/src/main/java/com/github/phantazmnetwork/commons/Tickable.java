package com.github.phantazmnetwork.commons;

/**
 * An interface which allows implementations to have periodic behavior.
 */
@FunctionalInterface
public interface Tickable {
    /**
     * Executes a "tick".
     * This enables implementations to run periodic behavior.
     * @param time the number of milliseconds that have elapsed
     */
    void tick(long time);
}
