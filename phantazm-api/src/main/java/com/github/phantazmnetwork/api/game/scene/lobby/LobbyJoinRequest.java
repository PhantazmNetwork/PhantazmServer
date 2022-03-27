package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A join request for lobbies.
 */
public interface LobbyJoinRequest {

    /**
     * Gets the players in the request.
     * @return The players in the request
     */
    @NotNull Iterable<PlayerView> getPlayers();

    /**
     * Handles {@link Instance} used for the join.
     * @param instance The {@link Instance} the players are joining
     * @param instanceConfig Configuration for the {@link Instance}
     */
    void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig);

}
