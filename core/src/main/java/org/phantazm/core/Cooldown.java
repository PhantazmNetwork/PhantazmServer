package org.phantazm.core;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple, thread-safe cooldown based on {@link AtomicInteger}. Instances can be obtained by calling
 * {@link Cooldown#cooldown()} or {@link Cooldown#cooldown(int)}.
 */
public final class Cooldown {
    private final AtomicInteger counter;

    private Cooldown(int initial) {
        this.counter = new AtomicInteger(initial);
    }

    private static void checkTimer(int timer) {
        if (timer < 0) {
            throw new IllegalArgumentException("Negative timer value");
        }
    }

    /**
     * Creates a new Cooldown which is initially acquirable.
     *
     * @return a new Cooldown instance
     */
    public static @NotNull Cooldown cooldown() {
        return new Cooldown(0);
    }

    /**
     * Creates a new Cooldown with an initial timer.
     *
     * @param initial the initial timer
     * @return a new Cooldown instance
     * @throws IllegalArgumentException if {@code initial} < 0
     */
    public static @NotNull Cooldown cooldown(int initial) {
        checkTimer(initial);
        return new Cooldown(initial);
    }

    /**
     * Steps the counter. Should be called once every tick. Returns {@code true} for the tick that the cooldown
     * expires.
     *
     * @return true if this tick ended the cooldown, false otherwise
     */
    public boolean step() {
        return counter.getAndUpdate(current -> Math.max(0, current - 1)) == 1;
    }

    /**
     * If the cooldown is available, sets the cooldown to {@code timer}.
     *
     * @return true if the cooldown was available, false otherwise
     */
    public boolean takeCooldown(int timer) {
        checkTimer(timer);
        return this.counter.compareAndSet(0, timer);
    }

    /**
     * Immediately resets the timer back to zero.
     *
     * @return true if the timer wasn't already expired
     */
    public boolean reset() {
        return this.counter.getAndSet(0) != 0;
    }
}