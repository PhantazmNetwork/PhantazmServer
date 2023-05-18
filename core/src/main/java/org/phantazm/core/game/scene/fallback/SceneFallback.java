package org.phantazm.core.game.scene.fallback;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

/**
 * A fallback for scenes to route players to when necessary.
 */
@FunctionalInterface
public interface SceneFallback {

    /**
     * Handles a fallback for a {@link PlayerView}.
     *
     * @param player The {@link PlayerView} to handle
     * @return Whether the fallback was successful
     */
    boolean fallback(@NotNull PlayerView player);

}
