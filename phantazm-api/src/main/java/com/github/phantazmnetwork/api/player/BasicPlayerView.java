package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Basic implementation of a {@link PlayerView}.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicPlayerView implements PlayerView {

    private final ConnectionManager connectionManager;

    private final UUID playerUUID;

    /**
     * Creates a basic {@link PlayerView}.
     * @param connectionManager The {@link ConnectionManager} used to find {@link Player}s based on their {@link UUID}
     * @param playerUUID The {@link UUID} of the {@link Player} to store
     */
    public BasicPlayerView(@NotNull ConnectionManager connectionManager, @NotNull UUID playerUUID) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.playerUUID = Objects.requireNonNull(playerUUID, "playerUUID");
    }

    @Override
    public @NotNull UUID getUUID() {
        return playerUUID;
    }

    @Override
    public @NotNull Optional<Player> getPlayer() {
        return Optional.ofNullable(connectionManager.getPlayer(playerUUID));
    }

}
