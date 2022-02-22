package com.github.phantazmnetwork.api.game.scene.lobby;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record LobbyDispatchRequest(@NotNull Iterable<LobbyJoiner> players, @NotNull String targetLobbyName) {

    public LobbyDispatchRequest {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(targetLobbyName, "targetLobbyName");
    }

}
