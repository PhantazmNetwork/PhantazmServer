package org.phantazm.core.game.scene.fallback;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link SceneFallback} which delegates to multiple sub-{@link SceneFallback}s.
 */
public class CompositeFallback implements SceneFallback {

    private final Iterable<SceneFallback> fallbacks;

    /**
     * Creates a composite {@link SceneFallback}.
     *
     * @param fallbacks The {@link SceneFallback}s to use for the fallback. This will iterate over the fallbacks until a
     *                  fallback succeeds.
     */
    public CompositeFallback(@NotNull Iterable<SceneFallback> fallbacks) {
        this.fallbacks = Objects.requireNonNull(fallbacks);
    }

    @Override
    public CompletableFuture<Boolean> fallback(@NotNull PlayerView player) {
        Iterator<SceneFallback> iterator = fallbacks.iterator();
        if (!iterator.hasNext()) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<Boolean> future = iterator.next().fallback(player);
        while (iterator.hasNext()) {
            SceneFallback fallback = iterator.next();
            future = future.thenCompose(result -> {
                if (result) {
                    return CompletableFuture.completedFuture(true);
                }

                return fallback.fallback(player);
            });
        }

        return future;
    }

}
