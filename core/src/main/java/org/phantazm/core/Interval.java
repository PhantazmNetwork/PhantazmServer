package org.phantazm.core;

import org.jetbrains.annotations.NotNull;

/**
 * A very basic utility class for creating periodic behavior. Not thread-safe, as it is expected to be called from a
 * tick thread only.
 */
public final class Interval {
    private final int interval;

    private int tick;

    private Interval(int interval) {
        this.interval = interval;
    }

    /**
     * Creates a new instance of this class.
     *
     * @param interval the interval, in ticks; cannot be negative
     * @return a new Interval instance
     * @throws IllegalArgumentException if {@code interval < 0}
     */
    public static @NotNull Interval of(int interval) {
        if (interval < 0) throw new IllegalArgumentException("interval cannot be less than 0");
        return new Interval(interval);
    }

    /**
     * Advances this interval; returns true iff this interval has elapsed. Should be called every tick.
     *
     * @return true if elapsed; false otherwise
     */
    public boolean advance() {
        if (this.tick++ >= interval) {
            this.tick = 0;
            return true;
        }

        return false;
    }
}
