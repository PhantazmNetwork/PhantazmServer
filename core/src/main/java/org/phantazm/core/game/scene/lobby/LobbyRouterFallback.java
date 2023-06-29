package org.phantazm.core.game.scene.lobby;

import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerView;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<Boolean> fallback(@NotNull PlayerView player) {
        LobbyJoinRequest joinRequest = new BasicLobbyJoinRequest(connectionManager, Collections.singleton(player));
        LobbyRouteRequest routeRequest = new LobbyRouteRequest(lobbyName, joinRequest);
        Optional<CompletableFuture<Lobby>> lobbyOptional = lobbyRouter.findScene(routeRequest).sceneFuture();
        if (lobbyOptional.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return lobbyOptional.get().thenApply(lobby -> {
            TransferResult result = lobby.join(joinRequest);
            if (result.executor().isEmpty()) {
                return false;
            }

            result.executor().get().run();
            return true;
        });
    }

}
