package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public interface SceneJoinRequest {
    /**
     * Gets an unmodifiable view of the players in the request.
     * @return An unmodifiable view of the players in the request
     */
    @UnmodifiableView @NotNull Collection<PlayerView> getPlayers();
}
