package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneJoinRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A request to be routed to a {@link Lobby}.
 */
public record LobbyRouteRequest(@NotNull String targetLobbyName, @NotNull LobbyJoinRequest joinRequest)
        implements SceneJoinRequest {

    /**
     * Creates a lobby route request.
     *
     * @param targetLobbyName The name of the lobby to join
     * @param joinRequest     The {@link LobbyJoinRequest} to be routed
     */
    public LobbyRouteRequest {
        Objects.requireNonNull(targetLobbyName, "targetLobbyName");
        Objects.requireNonNull(joinRequest, "joinRequest");
    }

    @Override
    public int getRequestWeight() {
        return joinRequest().getRequestWeight();
    }
}
