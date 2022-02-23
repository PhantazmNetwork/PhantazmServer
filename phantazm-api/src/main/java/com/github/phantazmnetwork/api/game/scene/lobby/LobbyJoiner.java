package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record LobbyJoiner(@NotNull PlayerView player) {

    public LobbyJoiner {
        Objects.requireNonNull(player, "player");
    }

}
