package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A request to be routed to a {@link Lobby}.
 * @param players The players that are being routed
 * @param targetLobbyName The name of the lobby to join
 */
public record LobbyRouteRequest(@NotNull Iterable<PlayerView> players, @NotNull String targetLobbyName) {

    public LobbyRouteRequest {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(targetLobbyName, "targetLobbyName");
    }

}
