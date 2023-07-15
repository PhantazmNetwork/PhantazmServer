package org.phantazm.core.game.scene.lobby;

import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
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

    private final PlayerLoginEvent event;

    private final PlayerViewProvider viewProvider;

    /**
     * Creates a {@link LobbyJoinRequest} for a newly joining player.
     *
     * @param event        The {@link PlayerLoginEvent} associated with the new player
     * @param viewProvider The {@link PlayerViewProvider} for the server
     */
    public LoginLobbyJoinRequest(@NotNull PlayerLoginEvent event, @NotNull PlayerViewProvider viewProvider) {
        this.event = Objects.requireNonNull(event, "event");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return List.of(viewProvider.fromPlayer(event.getPlayer()));
    }

    @Override
    public void handleJoin(@NotNull Lobby lobby, @NotNull Instance instance, @NotNull InstanceConfig instanceConfig) {
        event.setSpawningInstance(instance);
        event.getPlayer().setRespawnPoint(instanceConfig.spawnPoint());
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }
}
