package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A join request for lobbies.
 * @param players The players that are joining
 */
public record LobbyJoinRequest(@NotNull Iterable<PlayerView> players) {

    public LobbyJoinRequest {
        Objects.requireNonNull(players, "players");
    }

}
