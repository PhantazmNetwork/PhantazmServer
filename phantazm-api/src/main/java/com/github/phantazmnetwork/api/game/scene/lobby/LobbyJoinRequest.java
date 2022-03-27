package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;

/**
 * A join request for lobbies.
 */
public interface LobbyJoinRequest {

    /**
     * Gets an unmodifiable view of the players in the request.
     * @return An unmodifiable view of the players in the request
     */
    @UnmodifiableView @NotNull Collection<PlayerView> getPlayers();

    /**
     * Handles {@link Instance} used for the join.
     * @param instance The {@link Instance} the players are joining
     * @param instanceConfig Configuration for the {@link Instance}
     */
    void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig);

}
