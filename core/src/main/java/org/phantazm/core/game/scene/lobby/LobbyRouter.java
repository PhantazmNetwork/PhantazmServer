package org.phantazm.core.game.scene.lobby;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;

import java.util.*;

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
    public @NotNull Optional<Lobby> getCurrentScene(@NotNull UUID playerUUID) {
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            for (Lobby lobby : sceneProvider.getScenes()) {
                if (lobby.getPlayers().containsKey(playerUUID)) {
                    return Optional.of(lobby);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Collection<Lobby> getScenesContainingPlayer(@NotNull UUID playerUUID) {
        return getCurrentScene(playerUUID).map(Collections::singleton).orElse(Collections.emptySet());
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
            return RouteResult.failure(
                    Component.text("No lobbies exist under the name " + routeRequest.targetLobbyName() + "."));
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
    public boolean isGame() {
        return false;
    }

    @Override
    public boolean hasActiveScenes() {
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            if (sceneProvider.hasActiveScenes()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void tick(long time) {
        for (SceneProvider<Lobby, LobbyJoinRequest> sceneProvider : lobbyProviders.values()) {
            sceneProvider.tick(time);
        }
    }

}
