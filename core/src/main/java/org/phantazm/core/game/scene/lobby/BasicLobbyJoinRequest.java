package org.phantazm.core.game.scene.lobby;

import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
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
@SuppressWarnings("ClassCanBeRecord")
public class BasicLobbyJoinRequest implements LobbyJoinRequest {

    private final Collection<PlayerView> players;

    /**
     * Creates a basic {@link LobbyJoinRequest}.
     *
     * @param players The players in the request
     */
    public BasicLobbyJoinRequest(@NotNull Collection<PlayerView> players) {
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return players;
    }

    @Override
    public void handleJoin(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        for (PlayerView playerView : players) {
            playerView.getPlayer().ifPresent(player -> {
                player.setInstance(instance, instanceConfig.spawnPoint()).thenRun(() -> {
                    player.updateViewableRule(otherPlayer -> otherPlayer.getInstance() == instance);
                    player.setSkin(player.getSkin());
                });
                player.setGameMode(GameMode.ADVENTURE);
            });
        }
    }

}
