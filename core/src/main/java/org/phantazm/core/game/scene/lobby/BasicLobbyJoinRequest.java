package org.phantazm.core.game.scene.lobby;

import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.game.scene.Utils;
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

    private final Collection<PlayerView> players;

    /**
     * Creates a basic {@link LobbyJoinRequest}.
     *
     * @param players The players in the request
     */
    public BasicLobbyJoinRequest(@NotNull Collection<PlayerView> players) {
        this.players = Objects.requireNonNull(players, "players");
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return players;
    }

    @Override
    public void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        List<CompletableFuture<?>> futures = new ArrayList<>(players.size());
        for (PlayerView view : players) {
            view.getPlayer().ifPresent(player -> {
                player.setGameMode(GameMode.ADVENTURE);

                if (player.getInstance() == instance) {
                    futures.add(player.teleport(instanceConfig.spawnPoint()));
                }
                else {
                    Instance oldInstance = player.getInstance();
                    player.setInstanceAddCallback(() -> Utils.handleInstanceTransfer(oldInstance, instance, player));
                    futures.add(player.setInstance(instance, instanceConfig.spawnPoint()));
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

}
