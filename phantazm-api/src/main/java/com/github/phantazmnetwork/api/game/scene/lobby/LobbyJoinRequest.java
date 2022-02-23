package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record LobbyJoinRequest(@NotNull Iterable<PlayerView> players) {

    public LobbyJoinRequest {
        Objects.requireNonNull(players, "players");
    }

}
