package org.phantazm.core.game.scene.lobby;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.game.scene.*;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link Scene} router for {@link Lobby}s.
 */
public class LobbyRouter implements SceneRouter<Lobby, LobbyRouteRequest> {

    private final Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders;

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
    public @NotNull RouteResult<Lobby> findScene(@NotNull LobbyRouteRequest routeRequest) {
        if (isShutdown()) {
            return RouteResult.failure(Component.text("The router is shutdown."));
        }
        if (!isJoinable()) {
            return RouteResult.failure(Component.text("The router is not joinable."));
        }

        SceneProvider<Lobby, LobbyJoinRequest> lobbyProvider = lobbyProviders.get(routeRequest.targetLobbyName());
        if (lobbyProvider == null) {
            return RouteResult.failure(Component.text("No lobbies exist under the name " + routeRequest.targetLobbyName() + "."));
        }

        return lobbyProvider.provideScene(routeRequest.joinRequest()).map(RouteResult::success)
                .orElseGet(() -> RouteResult.failure(Component.text("No lobbies are joinable.")));
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
