package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.player.BasicPlayerView;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

/**
 * A {@link LobbyJoinRequest} used for when players initially join the server.
 */
public class LoginLobbyJoinRequest implements LobbyJoinRequest {

    private final PlayerLoginEvent event;

    private final PlayerView playerView;

    /**
     * Creates a {@link LobbyJoinRequest} for a newly joining player.
     * @param event The {@link PlayerLoginEvent} associated with the new player
     * @param connectionManager The {@link ConnectionManager} for the server
     */
    public LoginLobbyJoinRequest(@NotNull PlayerLoginEvent event, @NotNull ConnectionManager connectionManager) {
        this.event = Objects.requireNonNull(event, "event");
        this.playerView = new BasicPlayerView(Objects.requireNonNull(connectionManager, "connectionManager"),
                event.getPlayer().getUuid());
    }

    @Override
    public @NotNull Iterable<PlayerView> getPlayers() {
        return Collections.singleton(playerView);
    }

    @Override
    public void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        event.setSpawningInstance(instance);
        event.getPlayer().setRespawnPoint(instanceConfig.spawnPoint());
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

}
