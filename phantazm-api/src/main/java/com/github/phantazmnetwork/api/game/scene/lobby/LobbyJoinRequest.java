package com.github.phantazmnetwork.api.game.scene.lobby;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record LobbyJoinRequest(@NotNull Iterable<LobbyJoiner> players) {

    public LobbyJoinRequest {
        Objects.requireNonNull(players, "players");
    }

}
