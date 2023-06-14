package org.phantazm.core.game.scene.lobby;

import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerView;

import java.util.List;
import java.util.Objects;

/**
 * A {@link SceneFallback} which routes to a lobby.
 */
public class LobbyRouterFallback implements SceneFallback {

    private final ConnectionManager connectionManager;

    private final LobbyRouter lobbyRouter;

    private final String lobbyName;

    /**
     * Creates a lobby router fallback.
     *
     * @param lobby     The {@link LobbyRouter} to fallback to
     * @param lobbyName The name of the {@link Lobby} to fallback to
     */
    public LobbyRouterFallback(@NotNull ConnectionManager connectionManager, @NotNull LobbyRouter lobby,
            @NotNull String lobbyName) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.lobbyRouter = Objects.requireNonNull(lobby, "lobbyRouter");
        this.lobbyName = Objects.requireNonNull(lobbyName, "lobbyName");
    }

    @Override
    public boolean fallback(@NotNull PlayerView player) {
        return lobbyRouter.join(
                        new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(connectionManager, List.of(player))))
                .success();
    }

}
