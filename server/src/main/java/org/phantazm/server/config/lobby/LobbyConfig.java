package org.phantazm.server.config.lobby;

import com.github.steanky.ethylene.core.collection.ConfigList;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.game.scene.lobby.Lobby;

import java.util.List;
import java.util.Objects;

/**
 * Config for a single {@link Lobby}.
 *
 * @param instanceConfig The {@link InstanceConfig} for the {@link Lobby}
 * @param lobbyPaths     The lobby paths used to load the {@link Instance} for the lobby
 * @param maxPlayers     The maximum players possible for the {@link Lobby}
 * @param maxLobbies     The maximum number of {@link Lobby}s for this {@link Lobby} type
 */
public record LobbyConfig(@NotNull InstanceConfig instanceConfig,
                          @NotNull List<String> lobbyPaths,
                          int maxPlayers,
                          int maxLobbies,
                          @NotNull ConfigList npcs) {

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
     *
     * @param instanceConfig The {@link InstanceConfig} for the {@link Lobby}
     * @param lobbyPaths     The lobby paths used to load the {@link Instance} for the lobby
     * @param maxPlayers     The maximum players possible for the {@link Lobby}
     * @param maxLobbies     The maximum number of {@link Lobby}s for this {@link Lobby} type
     */
    public LobbyConfig {
        Objects.requireNonNull(instanceConfig, "instanceConfig");
        Objects.requireNonNull(lobbyPaths, "lobbyPaths");
    }

}
