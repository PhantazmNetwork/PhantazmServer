package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class FutureUtils {
    private static final CompletableFuture<?> NULL_COMPLETED_FUTURE = CompletableFuture.completedFuture(null);

    @SuppressWarnings("unchecked")
    public static <T> @NotNull CompletableFuture<T> nullCompletedFuture() {
        return (CompletableFuture<T>) NULL_COMPLETED_FUTURE;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull CompletableFuture<T> completedFuture(@Nullable T result) {
        return result == null ? ((CompletableFuture<T>) NULL_COMPLETED_FUTURE) :
            CompletableFuture.completedFuture(result);
    }
}
