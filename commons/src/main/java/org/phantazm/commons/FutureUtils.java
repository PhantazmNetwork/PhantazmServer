package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class FutureUtils {
    private static class UnobtrudableFuture<T> extends CompletableFuture<T> {
        public <U> CompletableFuture<U> newIncompleteFuture() {
            return new UnobtrudableFuture<>();
        }

        @Override
        public void obtrudeValue(T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void obtrudeException(Throwable ex) {
            throw new UnsupportedOperationException();
        }
    }

    private static final CompletableFuture<?> NULL_COMPLETED_FUTURE;
    private static final CompletableFuture<Boolean> TRUE_COMPLETED_FUTURE;
    private static final CompletableFuture<Boolean> FALSE_COMPLETED_FUTURE;

    static {
        CompletableFuture<?> nullFuture = new UnobtrudableFuture<>();
        CompletableFuture<Boolean> trueFuture = new UnobtrudableFuture<>();
        CompletableFuture<Boolean> falseFuture = new UnobtrudableFuture<>();

        nullFuture.complete(null);
        trueFuture.complete(true);
        falseFuture.complete(false);

        NULL_COMPLETED_FUTURE = nullFuture;
        TRUE_COMPLETED_FUTURE = trueFuture;
        FALSE_COMPLETED_FUTURE = falseFuture;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull CompletableFuture<T> nullCompletedFuture() {
        return (CompletableFuture<T>) NULL_COMPLETED_FUTURE;
    }

    public static @NotNull CompletableFuture<Boolean> trueCompletedFuture() {
        return TRUE_COMPLETED_FUTURE;
    }

    public static @NotNull CompletableFuture<Boolean> falseCompletedFuture() {
        return FALSE_COMPLETED_FUTURE;
    }

    public static <T> @NotNull CompletableFuture<T> completedFuture(@Nullable T object) {
        if (object == null) {
            return nullCompletedFuture();
        }

        CompletableFuture<T> future = new UnobtrudableFuture<>();
        future.complete(object);

        return future;
    }
}
