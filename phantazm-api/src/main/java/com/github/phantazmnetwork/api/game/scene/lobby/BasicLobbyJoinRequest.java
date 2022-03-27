package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic implementation of a {@link LobbyJoinRequest}.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicLobbyJoinRequest implements LobbyJoinRequest {

    private final Iterable<PlayerView> players;

    /**
     * Creates a basic {@link LobbyJoinRequest}.
     * @param players The players in the request
     */
    public BasicLobbyJoinRequest(@NotNull Iterable<PlayerView> players) {
        this.players = Objects.requireNonNull(players, "players");
    }

    @Override
    public @NotNull Iterable<PlayerView> getPlayers() {
        return players;
    }

    @Override
    public void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        for (PlayerView playerView : players) {
            playerView.getPlayer().ifPresent(player -> {
                player.setInstance(instance, instanceConfig.spawnPoint());
                player.setGameMode(GameMode.ADVENTURE);
            });
        }
    }

}
