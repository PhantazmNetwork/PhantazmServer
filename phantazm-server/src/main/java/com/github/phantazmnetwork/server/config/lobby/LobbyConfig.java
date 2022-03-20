package com.github.phantazmnetwork.server.config.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for a single {@link com.github.phantazmnetwork.api.game.scene.lobby.Lobby}.
 */
public record LobbyConfig(@NotNull InstanceConfig instanceConfig,
                          @NotNull String[] lobbyPaths,
                          int maxPlayers,
                          int maxLobbies) {

    /**
     * The default number of max players.
     */
    public static final int DEFAULT_MAX_PLAYERS = 10;

    /**
     * The default number of max lobbies.
     */
    public static final int DEFAULT_MAX_LOBBIES = 10;

    /**
     * Creates a lobby config.
     * @param instanceConfig The {@link InstanceConfig} for the {@link com.github.phantazmnetwork.api.game.scene.lobby.Lobby}
     * @param lobbyPaths The lobby paths used to load the {@link net.minestom.server.instance.Instance} for the lobby
     * @param maxPlayers The maximum players possible for the {@link com.github.phantazmnetwork.api.game.scene.lobby.Lobby}
     * @param maxLobbies The maximum number of {@link com.github.phantazmnetwork.api.game.scene.lobby.Lobby}s for this
     *                   {@link com.github.phantazmnetwork.api.game.scene.lobby.Lobby} type
     */
    public LobbyConfig {
        Objects.requireNonNull(instanceConfig, "instanceConfig");
        Objects.requireNonNull(lobbyPaths, "lobbyPaths");
    }

}
