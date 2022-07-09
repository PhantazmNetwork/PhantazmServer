package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.game.scene.SceneJoinRequest;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

/**
 * A join request for lobbies.
 */
public interface LobbyJoinRequest extends SceneJoinRequest {
    /**
     * Handles {@link Instance} used for the join.
     * @param instance The {@link Instance} the players are joining
     * @param instanceConfig Configuration for the {@link Instance}
     */
    void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig);
}