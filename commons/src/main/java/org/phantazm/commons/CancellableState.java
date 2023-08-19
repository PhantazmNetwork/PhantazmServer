package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public interface CancellableState {
    void start();

    void end();

    @NotNull
    UUID id();

    static @NotNull CancellableState named(@NotNull UUID id, @NotNull Runnable start, @NotNull Runnable end) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

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
            public @NotNull UUID id() {
                return id;
            }
        };
    }
}
