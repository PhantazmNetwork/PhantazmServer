package com.github.phantazmnetwork.api.game.scene.fallback;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

/**
 * A fallback for scenes to route players to when necessary.
 */
@FunctionalInterface
public interface SceneFallback {

    /**
     * Handles a fallback for a {@link PlayerView}.
     * @param player The {@link PlayerView} to handle
     * @return Whether the fallback was successful
     */
    boolean fallback(@NotNull PlayerView player);

}
