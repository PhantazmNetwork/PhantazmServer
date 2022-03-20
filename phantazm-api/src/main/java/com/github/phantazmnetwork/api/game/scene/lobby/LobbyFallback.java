package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

/**
 * A {@link SceneFallback} which routes to a lobby.
 */
@SuppressWarnings("ClassCanBeRecord")
public class LobbyFallback implements SceneFallback {

    private final Lobby lobby;

    /**
     * Creates a lobby fallback.
     * @param lobby The lobby to fallback to
     */
    public LobbyFallback(@NotNull Lobby lobby) {
        this.lobby = Objects.requireNonNull(lobby, "lobby");
    }

    @Override
    public boolean fallback(@NotNull PlayerView player) {
        return lobby.join(new LobbyJoinRequest(Collections.singletonList(player))).success();
    }

}
