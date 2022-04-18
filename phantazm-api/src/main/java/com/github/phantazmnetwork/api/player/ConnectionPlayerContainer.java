package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Creates a {@link PlayerContainer} based on a {@link ConnectionManager}.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ConnectionPlayerContainer implements PlayerContainer {

    private final ConnectionManager connectionManager;

    /**
     * Creates a new {@link ConnectionPlayerContainer}.
     * @param connectionManager The {@link ConnectionManager} to use
     */
    public ConnectionPlayerContainer(@NotNull ConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
    }

    @Override
    public @Nullable Player getPlayer(@NotNull UUID uuid) {
        return connectionManager.getPlayer(uuid);
    }

    @Override
    public @Nullable Player getPlayer(@NotNull String name) {
        return connectionManager.getPlayer(name);
    }

}
