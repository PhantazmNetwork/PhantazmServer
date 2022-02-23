package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.dispatcher.DispatchResult;
import com.github.phantazmnetwork.api.game.dispatcher.SceneDispatcher;
import com.github.phantazmnetwork.api.game.scene.JoinResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class LobbyDispatcher implements SceneDispatcher<LobbyDispatchRequest> {

    private final Map<String, SceneProvider<Lobby>> sceneProviders;

    private boolean joinable = true;

    public LobbyDispatcher(@NotNull Map<String, SceneProvider<Lobby>> sceneProviders) {
        this.sceneProviders = sceneProviders;
    }

    @Override
    public @NotNull DispatchResult dispatch(@NotNull LobbyDispatchRequest dispatchRequest) {
        if (!joinable) {
            return new DispatchResult(false, Optional.of("The dispatcher is not joinable."));
        }

        SceneProvider<Lobby> sceneProvider = sceneProviders.get(dispatchRequest.targetLobbyName());
        if (sceneProvider == null) {
            return new DispatchResult(false,
                    Optional.of("No lobbies exist under the name " + dispatchRequest.targetLobbyName() + "."));
        }

        JoinResult result = sceneProvider.provideScene().join(new LobbyJoinRequest(dispatchRequest.players()));

        return new DispatchResult(result.success(), result.message());
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public void forceShutdown() {
        for (SceneProvider<Lobby> sceneProvider : sceneProviders.values()) {
            sceneProvider.forceShutdown();
        }
    }

    @Override
    public void tick() {
        for (SceneProvider<Lobby> group : sceneProviders.values()) {
            group.tick();
        }
    }

}
