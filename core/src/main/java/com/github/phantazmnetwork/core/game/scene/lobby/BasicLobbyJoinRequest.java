package com.github.phantazmnetwork.core.game.scene.lobby;

import com.github.phantazmnetwork.core.config.InstanceConfig;
import com.github.phantazmnetwork.core.player.PlayerView;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Basic implementation of a {@link LobbyJoinRequest}.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicLobbyJoinRequest implements LobbyJoinRequest {

    private final Collection<PlayerView> players;

    /**
     * Creates a basic {@link LobbyJoinRequest}.
     *
     * @param players The players in the request
     */
    public BasicLobbyJoinRequest(@NotNull Collection<PlayerView> players) {
        this.players = Collections.unmodifiableCollection(Objects.requireNonNull(players, "players"));
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
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
