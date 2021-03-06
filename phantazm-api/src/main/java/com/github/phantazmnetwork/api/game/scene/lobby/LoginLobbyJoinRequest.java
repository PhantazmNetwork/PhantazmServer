package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * A {@link LobbyJoinRequest} used for when players initially join the server.
 */
public class LoginLobbyJoinRequest implements LobbyJoinRequest {

    private final PlayerLoginEvent event;

    private final PlayerViewProvider viewProvider;

    /**
     * Creates a {@link LobbyJoinRequest} for a newly joining player.
     * @param event The {@link PlayerLoginEvent} associated with the new player
     * @param viewProvider The {@link PlayerViewProvider} for the server
     */
    public LoginLobbyJoinRequest(@NotNull PlayerLoginEvent event, @NotNull PlayerViewProvider viewProvider) {
        this.event = Objects.requireNonNull(event, "event");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
    }

    @Override
    public @UnmodifiableView @NotNull Collection<PlayerView> getPlayers() {
        return Collections.singleton(viewProvider.fromPlayer(event.getPlayer()));
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
        // NO-OP
    }

}
