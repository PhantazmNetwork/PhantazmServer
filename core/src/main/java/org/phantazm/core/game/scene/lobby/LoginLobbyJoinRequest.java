package org.phantazm.core.game.scene.lobby;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A {@link LobbyJoinRequest} used for when players initially join the server.
 */
public class LoginLobbyJoinRequest implements LobbyJoinRequest {

    private final ConnectionManager connectionManager;

    private final PlayerLoginEvent event;

    private final PlayerViewProvider viewProvider;

    /**
     * Creates a {@link LobbyJoinRequest} for a newly joining player.
     *
     * @param event        The {@link PlayerLoginEvent} associated with the new player
     * @param viewProvider The {@link PlayerViewProvider} for the server
     */
    public LoginLobbyJoinRequest(@NotNull ConnectionManager connectionManager, @NotNull PlayerLoginEvent event,
            @NotNull PlayerViewProvider viewProvider) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.event = Objects.requireNonNull(event, "event");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return List.of(viewProvider.fromPlayer(event.getPlayer()));
    }

    @Override
    public void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        event.setSpawningInstance(instance);
        event.getPlayer().setRespawnPoint(instanceConfig.spawnPoint());
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    /**
     * Handles when the player's login is complete.
     * This should be fired from a handler to a {@link PlayerSpawnEvent}, as this occurs when a player
     * has spawned for the first time.
     */
    public void onPlayerLoginComplete() {
        event.getPlayer().updateViewableRule(otherPlayer -> otherPlayer.getInstance() == event.getSpawningInstance());
        for (Player player : connectionManager.getOnlinePlayers()) {
            if (player.getInstance() != event.getSpawningInstance()) {
                player.sendPacket(event.getPlayer().getRemovePlayerToList());
                event.getPlayer().sendPacket(player.getRemovePlayerToList());
            }
        }
    }

}
