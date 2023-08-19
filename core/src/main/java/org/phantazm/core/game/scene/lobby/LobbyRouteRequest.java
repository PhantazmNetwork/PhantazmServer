package org.phantazm.core.game.scene.lobby;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneJoinRequest;

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
        Objects.requireNonNull(targetLobbyName);
        Objects.requireNonNull(joinRequest);
    }

    @Override
    public int getRequestWeight() {
        return joinRequest().getRequestWeight();
    }
}
