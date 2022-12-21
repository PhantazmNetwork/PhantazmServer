package org.phantazm.core.game.scene.fallback;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Objects;

/**
 * A {@link SceneFallback} which delegates to multiple sub-{@link SceneFallback}s.
 */
public class CompositeFallback implements SceneFallback {

    private final Iterable<SceneFallback> fallbacks;

    /**
     * Creates a composite {@link SceneFallback}.
     *
     * @param fallbacks The {@link SceneFallback}s to use for the fallback. This will iterate over the fallbacks until
     *                  a fallback succeeds.
     */
    public CompositeFallback(@NotNull Iterable<SceneFallback> fallbacks) {
        this.fallbacks = Objects.requireNonNull(fallbacks, "fallbacks");
    }

    @Override
    public boolean fallback(@NotNull PlayerView player) {
        for (SceneFallback fallback : fallbacks) {
            if (fallback.fallback(player)) {
                return true;
            }
        }

        return false;
    }

}
