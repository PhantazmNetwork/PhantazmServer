package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record LobbyRouteRequest(@NotNull Iterable<PlayerView> players, @NotNull String targetLobbyName) {

    public LobbyRouteRequest {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(targetLobbyName, "targetLobbyName");
    }

}
