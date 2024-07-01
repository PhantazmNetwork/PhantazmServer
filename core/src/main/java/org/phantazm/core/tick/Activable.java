package org.phantazm.core.tick;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Activable extends Tickable {

    default void start() {

    }

    default void tick(long time) {

    }

    default void end() {

    }

    /**
     * Given a normal activable, creates a new thread-safe activable whose start() methods will not activate if the
     * activable is already active. Conversely, the end() method will not be invoked if the activable is not activated.
     * Similarly, the tick(long) method will only be called if the activable is currently active.
     *
     * @param other the activable to wrap
     * @return the thread-safe wrapper instance
     */
    static @NotNull Activable threadsafeWrapper(@NotNull Activable other) {
        return new Activable() {
            private final AtomicBoolean started = new AtomicBoolean();

            @Override
            public void start() {
                if (!started.compareAndExchange(false, true)) {
                    other.start();
                }
            }

            @Override
            public void tick(long time) {
                if (started.get()) {
                    other.tick(time);
                }
            }

            @Override
            public void end() {
                if (started.compareAndExchange(true, false)) {
                    other.end();
                }
            }
        };
    }
}
