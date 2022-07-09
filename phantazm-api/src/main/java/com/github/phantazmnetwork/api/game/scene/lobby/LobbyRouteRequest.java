package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneJoinRequest;
import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;

/**
 * A request to be routed to a {@link Lobby}.
 */
public record LobbyRouteRequest(@NotNull String targetLobbyName, @NotNull LobbyJoinRequest joinRequest)
        implements SceneJoinRequest {

    /**
     * Creates a lobby route request.
     * @param targetLobbyName The name of the lobby to join
     * @param joinRequest The {@link LobbyJoinRequest} to be routed
     */
    public LobbyRouteRequest {
        Objects.requireNonNull(targetLobbyName, "targetLobbyName");
        Objects.requireNonNull(joinRequest, "joinRequest");
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return joinRequest.getPlayers();
    }
}
