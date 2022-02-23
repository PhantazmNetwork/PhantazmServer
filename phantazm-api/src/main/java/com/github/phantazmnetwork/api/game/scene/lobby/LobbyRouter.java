package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.router.SceneRouter;
import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LobbyRouter implements SceneRouter<LobbyRouteRequest> {

    private final Map<String, SceneProvider<Lobby>> lobbyProviders;

    private final Map<UUID, Lobby> lobbyMap = new HashMap<>();

    private boolean shutdown = false;

    private boolean joinable = true;

    public LobbyRouter(@NotNull Map<String, SceneProvider<Lobby>> lobbyProviders) {
        this.lobbyProviders = Objects.requireNonNull(lobbyProviders, "lobbyProviders");
    }

    @Override
    public @NotNull RouteResult join(@NotNull LobbyRouteRequest routeRequest) {
        if (!joinable) {
            return new RouteResult(false, Optional.of("The router is not joinable."));
        }

        SceneProvider<Lobby> lobbyProvider = lobbyProviders.get(routeRequest.targetLobbyName());
        if (lobbyProvider == null) {
            return new RouteResult(false,
                    Optional.of("No lobbies exist under the name " + routeRequest.targetLobbyName() + "."));
        }

        Lobby lobby = lobbyProvider.provideScene();
        RouteResult result = lobby.join(new LobbyJoinRequest(routeRequest.players()));
        if (result.success()) {
            for (PlayerView playerView : routeRequest.players()) {
                lobbyMap.put(playerView.getUUID(), lobby);
            }
        }

        return result;
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID uuid : leavers) {
            if (!lobbyMap.containsKey(uuid)) {
                return new RouteResult(false,
                        Optional.of(uuid + " is not part of a scene in the lobby router."));
            }
        }

        for (UUID uuid : leavers) {
            lobbyMap.get(uuid).leave(Collections.singletonList(uuid));
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        // TODO: use UUID-based connection-manager
        Map<UUID, PlayerView> players = new HashMap<>();

        for (SceneProvider<Lobby> lobbyProvider : lobbyProviders.values()) {
            for (Lobby lobby : lobbyProvider.listScenes()) {
                players.putAll(lobby.getPlayers());
            }
        }

        return players;
    }

    @Override
    public int getIngamePlayerCount() {
        int playerCount = 0;

        for (SceneProvider<Lobby> lobbyProvider : lobbyProviders.values()) {
            for (Lobby lobby : lobbyProvider.listScenes()) {
                for (PlayerView ignored : lobby.getPlayers().values()) {
                    playerCount++;
                }
            }
        }

        return playerCount;
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
        for (SceneProvider<Lobby> lobbyProvider : lobbyProviders.values()) {
            lobbyProvider.forceShutdown();
        }

        shutdown = true;
    }

    @Override
    public void tick() {
        for (SceneProvider<Lobby> sceneProvider : lobbyProviders.values()) {
            sceneProvider.tick();
        }
    }

}
