package org.phantazm.core.game.scene.lobby;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link Scene} router for {@link Lobby}s.
 */
public class LobbyRouter implements SceneRouter<Lobby, LobbyRouteRequest> {
    private final UUID uuid;

    private final Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders;

    private boolean shutdown = false;

    private boolean joinable = true;

    /**
     * Creates a {@link Lobby} router.
     *
     * @param lobbyProviders The {@link SceneProvider}s for lobbies mapped based on lobby name.
     */
    public LobbyRouter(@NotNull UUID uuid,
            @NotNull Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.lobbyProviders = Objects.requireNonNull(lobbyProviders, "lobbyProviders");
    }

    @Override
    public @NotNull Collection<Lobby> getScenes() {
        Collection<Lobby> scenes = new ArrayList<>();
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            scenes.addAll(sceneProvider.getScenes());
        }

        return scenes;
    }

    @Override
    public @NotNull Optional<Lobby> getScene(@NotNull UUID uuid) {
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            for (Lobby lobby : sceneProvider.getScenes()) {
                if (lobby.getPlayers().containsKey(uuid)) {
                    return Optional.of(lobby);
                }
            }
        }

        return Optional.empty();
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
        return lobbyOptional.map(lobby -> lobby.join(joinRequest))
                .orElseGet(() -> new RouteResult(false, Component.text("No lobbies are joinable.")));

    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        List<Pair<Lobby, UUID>> scenes = new ArrayList<>();
        for (UUID uuid : leavers) {
            Optional<Lobby> sceneOptional = getScene(uuid);
            if (sceneOptional.isEmpty()) {
                return new RouteResult(false, Component.text(uuid + " is not part of a lobby in the Lobby router."));
            }

            scenes.add(Pair.of(sceneOptional.get(), uuid));
        }

        boolean success = true;
        for (Pair<Lobby, UUID> pair : scenes) {
            RouteResult subResult = pair.left().leave(Collections.singleton(pair.right()));

            if (!subResult.success()) {
                success = false;
            }
        }

        if (success) {
            return RouteResult.SUCCESSFUL;
        }

        return new RouteResult(false, Optional.of(Component.text("Failed to remove a player from a lobby.")));
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        return lobbyProviders.values().stream().flatMap(provider -> provider.getScenes().stream())
                .flatMap(lobby -> lobby.getPlayers().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
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
    public void shutdown() {
        for (SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider : lobbyProviders.values()) {
            lobbyProvider.forceShutdown();
        }

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
