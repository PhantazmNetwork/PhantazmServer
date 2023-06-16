package org.phantazm.core.game.scene.lobby;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.player.PlayerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Basic implementation of a {@link LobbyJoinRequest}.
 */
public class BasicLobbyJoinRequest implements LobbyJoinRequest {

    private static final CompletableFuture<?>[] EMPTY_COMPLETABLE_FUTURE_ARRAY = new CompletableFuture[0];

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
        List<Player> teleportedPlayers = new ArrayList<>(players.size());
        List<CompletableFuture<?>> futures = new ArrayList<>(players.size());
        for (PlayerView view : players) {
            view.getPlayer().ifPresent(player -> {
                player.setGameMode(GameMode.ADVENTURE);

                teleportedPlayers.add(player);
                if (player.getInstance() == instance) {
                    futures.add(player.teleport(instanceConfig.spawnPoint()));
                } else {
                    futures.add(player.setInstance(instance, instanceConfig.spawnPoint()));
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(EMPTY_COMPLETABLE_FUTURE_ARRAY)).thenRun(() -> {
            for (int i = 0; i < futures.size(); ++i) {
                Player teleportedPlayer = teleportedPlayers.get(i);

                for (Player otherPlayer : connectionManager.getOnlinePlayers()) {
                    if (otherPlayer.getInstance() != instance) {
                        otherPlayer.removeViewer(teleportedPlayer);
                        teleportedPlayer.sendPacket(otherPlayer.getRemovePlayerToList());
                        teleportedPlayer.removeViewer(otherPlayer);
                        otherPlayer.sendPacket(teleportedPlayer.getRemovePlayerToList());
                    } else {
                        teleportedPlayer.sendPacket(otherPlayer.getAddPlayerToList());
                        otherPlayer.addViewer(teleportedPlayer);
                        otherPlayer.sendPacket(teleportedPlayer.getAddPlayerToList());
                        teleportedPlayer.addViewer(otherPlayer);
                    }
                }
            }
        }).join();
    }

}
