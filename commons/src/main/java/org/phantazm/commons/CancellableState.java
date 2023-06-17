package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface CancellableState {
    void start();

    void end();

    @NotNull String id();

    static @NotNull CancellableState named(@NotNull String id, @NotNull Runnable start, @NotNull Runnable end) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");

        return new CancellableState() {
            @Override
            public void start() {
                start.run();
            }

            @Override
            public void end() {
                end.run();
            }

            @Override
            public @NotNull String id() {
                return id;
            }
        };
    }
}
