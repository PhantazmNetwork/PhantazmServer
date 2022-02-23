package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.dispatcher.DispatchResult;
import com.github.phantazmnetwork.api.game.dispatcher.SceneDispatcher;
import com.github.phantazmnetwork.api.game.scene.JoinResult;
import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LobbyDispatcher implements SceneDispatcher<LobbyDispatchRequest> {

    private final Map<String, SceneProvider<Lobby>> lobbyProviders;

    private boolean joinable = true;

    public LobbyDispatcher(@NotNull Map<String, SceneProvider<Lobby>> lobbyProviders) {
        this.lobbyProviders = Objects.requireNonNull(lobbyProviders, "lobbyProviders");
    }

    @Override
    public @NotNull DispatchResult dispatch(@NotNull LobbyDispatchRequest dispatchRequest) {
        if (!joinable) {
            return new DispatchResult(false, Optional.of("The dispatcher is not joinable."));
        }

        SceneProvider<Lobby> lobbyProvider = lobbyProviders.get(dispatchRequest.targetLobbyName());
        if (lobbyProvider == null) {
            return new DispatchResult(false,
                    Optional.of("No lobbies exist under the name " + dispatchRequest.targetLobbyName() + "."));
        }

        JoinResult result = lobbyProvider.provideScene().join(new LobbyJoinRequest(dispatchRequest.players()));

        return new DispatchResult(result.success(), result.message());
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
    }

    @Override
    public void tick() {
        for (SceneProvider<Lobby> group : lobbyProviders.values()) {
            group.tick();
        }
    }

}
