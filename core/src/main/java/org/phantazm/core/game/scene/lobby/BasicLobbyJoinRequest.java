package org.phantazm.core.game.scene.lobby;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.player.PlayerView;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Basic implementation of a {@link LobbyJoinRequest}.
 */
public class BasicLobbyJoinRequest implements LobbyJoinRequest {

    private final ConnectionManager connectionManager;

    private final Collection<PlayerView> players;

    /**
     * Creates a basic {@link LobbyJoinRequest}.
     *
     * @param players The players in the request
     */
    public BasicLobbyJoinRequest(@NotNull ConnectionManager connectionManager,
            @NotNull Collection<PlayerView> players) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.players = Objects.requireNonNull(players, "players");
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return players;
    }

    @Override
    public void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        for (PlayerView playerView : players) {
            playerView.getPlayer().ifPresent(player -> {
                if (player.getInstance() != instance) {
                    for (Player otherPlayer : instance.getPlayers()) {
                        otherPlayer.sendPacket(player.getAddPlayerToList());
                        player.sendPacket(otherPlayer.getAddPlayerToList());
                    }
                }

                player.setInstance(instance, instanceConfig.spawnPoint()).thenRun(() -> {
                    player.updateViewableRule(otherPlayer -> otherPlayer.getInstance() == instance);
                });
                player.setGameMode(GameMode.ADVENTURE);

                for (Player otherPlayer : connectionManager.getOnlinePlayers()) {
                    if (otherPlayer.getInstance() != instance) {
                        otherPlayer.sendPacket(player.getRemovePlayerToList());
                        player.sendPacket(otherPlayer.getRemovePlayerToList());
                    }
                }
            });
        }
    }

}
