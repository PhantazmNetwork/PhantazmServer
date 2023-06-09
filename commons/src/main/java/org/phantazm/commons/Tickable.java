package org.phantazm.commons;

/**
 * An interface which allows implementations to have periodic behavior.
 */
public interface Tickable {
    /**
     * Executes a "tick".
     * This enables implementations to run periodic behavior.
     *
     * @param time the number of milliseconds that have elapsed
     */
    default void tick(long time) {
    }
}
