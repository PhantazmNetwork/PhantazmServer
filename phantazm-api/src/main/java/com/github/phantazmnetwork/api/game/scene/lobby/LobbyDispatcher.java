package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.dispatcher.DispatchResult;
import com.github.phantazmnetwork.api.game.dispatcher.SceneDispatcher;
import com.github.phantazmnetwork.api.game.scene.JoinResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class LobbyDispatcher implements SceneDispatcher<LobbyDispatchRequest> {

    private final Map<String, LobbyGroup> lobbyGroups;

    private boolean joinable;

    public LobbyDispatcher(@NotNull Map<String, LobbyGroup> lobbyGroups) {
        this.lobbyGroups = lobbyGroups;
    }

    @Override
    public @NotNull DispatchResult dispatch(@NotNull LobbyDispatchRequest dispatchRequest) {
        if (!joinable) {
            return new DispatchResult(false, Optional.of("The dispatcher is not joinable."));
        }

        LobbyGroup lobbyGroup = lobbyGroups.get(dispatchRequest.targetLobbyName());
        if (lobbyGroup == null) {
            return new DispatchResult(false,
                    Optional.of("No lobbies exist under the name " + dispatchRequest.targetLobbyName() + "."));
        }

        JoinResult result = lobbyGroup.getLobby().join(new LobbyJoinRequest(dispatchRequest.players()));

        return new DispatchResult(result.success(), result.message());
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public void tick() {
        for (LobbyGroup group : lobbyGroups.values()) {
            group.tick();
        }
    }

}
