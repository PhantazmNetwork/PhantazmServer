package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.router.SceneRouter;
import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link SceneRouter} for lobbies.
 */
public class LobbyRouter implements SceneRouter<LobbyRouteRequest> {

    private final Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders;

    private final Map<UUID, Lobby> lobbyMap = new HashMap<>();

    private final Map<UUID, PlayerView> players = new HashMap<>();

    private final Map<UUID, PlayerView> unmodifiablePlayers = Collections.unmodifiableMap(players);

    private boolean shutdown = false;

    private boolean joinable = true;

    /**
     * Creates a lobby {@link SceneRouter}.
     * @param lobbyProviders The {@link SceneProvider}s for lobbies mapped based on lobby name.
     */
    public LobbyRouter(@NotNull Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders) {
        this.lobbyProviders = Objects.requireNonNull(lobbyProviders, "lobbyProviders");
    }

    @Override
    public @NotNull RouteResult join(@NotNull LobbyRouteRequest routeRequest) {
        if (!joinable) {
            return new RouteResult(false, Optional.of(Component.text("The router is not joinable.")));
        }

        SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider = lobbyProviders.get(routeRequest.targetLobbyName());
        if (lobbyProvider == null) {
            return new RouteResult(false,
                    Optional.of(Component.text("No lobbies exist under the name "
                            + routeRequest.targetLobbyName() + ".")));
        }

        LobbyJoinRequest joinRequest = new LobbyJoinRequest(routeRequest.players());
        Optional<Lobby> lobbyOptional = lobbyProvider.provideScene(joinRequest);
        if (lobbyOptional.isPresent()) {
            Lobby lobby = lobbyOptional.get();
            RouteResult result = lobby.join(joinRequest);
            if (result.success()) {
                for (PlayerView playerView : routeRequest.players()) {
                    lobbyMap.put(playerView.getUUID(), lobby);
                    players.put(playerView.getUUID(), playerView);
                }
            }

            return result;
        }

        return new RouteResult(false, Optional.of(Component.text("No lobbies are joinable.")));
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID uuid : leavers) {
            if (!lobbyMap.containsKey(uuid)) {
                return new RouteResult(false,
                        Optional.of(Component.text(uuid + " is not part of a scene in the lobby router.")));
            }
        }

        for (UUID uuid : leavers) {
            lobbyMap.get(uuid).leave(Collections.singletonList(uuid));
            players.remove(uuid);
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        return unmodifiablePlayers;
    }

    @Override
    public int getIngamePlayerCount() {
        int playerCount = 0;

        for (SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider : lobbyProviders.values()) {
            for (Lobby lobby : lobbyProvider.getScenes()) {
                playerCount += lobby.getPlayers().size();
            }
        }

        return playerCount;
    }

    @Override
    public int getJoinWeight(@NotNull LobbyRouteRequest request) {
        int count = 0;
        for (PlayerView ignored : request.players()) {
            count++;
        }
        return -(getIngamePlayerCount() + count);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public void forceShutdown() {
        for (SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider : lobbyProviders.values()) {
            lobbyProvider.forceShutdown();
        }
        lobbyMap.clear();
        players.clear();

        shutdown = true;
    }

    @Override
    public void tick() {
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            sceneProvider.tick();
        }
    }

}
