package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneFallback;
import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class LobbyFallback implements SceneFallback {

    private final Lobby lobby;

    public LobbyFallback(@NotNull Lobby lobby) {
        this.lobby = Objects.requireNonNull(lobby, "lobby");
    }

    @Override
    public void fallback(@NotNull PlayerView player) {
        lobby.join(new LobbyJoinRequest(Collections.singletonList(player)));
    }

}
