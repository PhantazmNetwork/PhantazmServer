package com.github.phantazmnetwork.core.game.scene.lobby;

import com.github.phantazmnetwork.core.game.scene.RouteResult;
import com.github.phantazmnetwork.core.game.scene.Scene;
import com.github.phantazmnetwork.core.game.scene.SceneProvider;
import com.github.phantazmnetwork.core.player.PlayerView;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * {@link Scene} router for {@link Lobby}s.
 */
public class LobbyRouter implements Scene<LobbyRouteRequest> {

    private final Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders;

    private final Map<UUID, Lobby> playerLobbyMap = new HashMap<>();

    private final Map<UUID, PlayerView> unmodifiablePlayers = new AbstractMap<>() {

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof UUID uuid)) {
                return false;
            }

            return playerLobbyMap.containsKey(uuid);
        }

        @Override
        public PlayerView get(Object key) {
            if (!(key instanceof UUID uuid)) {
                return null;
            }

            Lobby lobby = playerLobbyMap.get(uuid);
            if (lobby == null) {
                return null;
            }

            return lobby.getPlayers().get(uuid);
        }

        @NotNull
        @Override
        public Set<Entry<UUID, PlayerView>> entrySet() {
            Set<Entry<UUID, PlayerView>> entrySet = new HashSet<>();
            for (Lobby lobby : playerLobbyMap.values()) {
                entrySet.addAll(lobby.getPlayers().entrySet());
            }

            return Collections.unmodifiableSet(entrySet);
        }

    };

    private boolean shutdown = false;

    private boolean joinable = true;

    /**
     * Creates a {@link Lobby} router.
     *
     * @param lobbyProviders The {@link SceneProvider}s for lobbies mapped based on lobby name.
     */
    public LobbyRouter(@NotNull Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders) {
        this.lobbyProviders = Objects.requireNonNull(lobbyProviders, "lobbyProviders");
    }

    @Override
    public @NotNull RouteResult join(@NotNull LobbyRouteRequest routeRequest) {
        if (isShutdown()) {
            return new RouteResult(false, Component.text("The router is shutdown."));
        }
        if (!isJoinable()) {
            return new RouteResult(false, Component.text("The router is not joinable."));
        }

        SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider = lobbyProviders.get(routeRequest.targetLobbyName());
        if (lobbyProvider == null) {
            return new RouteResult(false,
                    Component.text("No lobbies exist under the name " + routeRequest.targetLobbyName() + "."));
        }

        LobbyJoinRequest joinRequest = routeRequest.joinRequest();
        Optional<Lobby> lobbyOptional = lobbyProvider.provideScene(joinRequest);
        if (lobbyOptional.isPresent()) {
            Lobby lobby = lobbyOptional.get();
            RouteResult result = lobby.join(joinRequest);
            if (result.success()) {
                for (PlayerView playerView : routeRequest.joinRequest().getPlayers()) {
                    Lobby oldLobby = playerLobbyMap.get(playerView.getUUID());
                    if (oldLobby != null && oldLobby != lobby) {
                        oldLobby.leave(Collections.singleton(playerView.getUUID()));
                    }

                    playerLobbyMap.put(playerView.getUUID(), lobby);
                }
            }

            return result;
        }

        return new RouteResult(false, Component.text("No lobbies are joinable."));
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID uuid : leavers) {
            if (!playerLobbyMap.containsKey(uuid)) {
                return new RouteResult(false, Component.text(uuid + " is not part of a scene in the lobby router."));
            }
        }

        for (UUID uuid : leavers) {
            playerLobbyMap.get(uuid).leave(Collections.singleton(uuid));
        }

        return RouteResult.SUCCESSFUL;
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
        return -(getIngamePlayerCount() + request.getRequestWeight());
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void forceShutdown() {
        for (SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider : lobbyProviders.values()) {
            lobbyProvider.forceShutdown();
        }
        playerLobbyMap.clear();

        shutdown = true;
    }

    @Override
    public boolean isJoinable() {
        return joinable;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public void tick(long time) {
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            sceneProvider.tick(time);
        }
    }

}
