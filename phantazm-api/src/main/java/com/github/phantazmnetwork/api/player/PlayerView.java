package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a view of a player that may be offline.
 */
public interface PlayerView {

    /**
     * Gets the {@link UUID} of the player.
     * @return The {@link UUID} of the player
     */
    @NotNull UUID getUUID();

    /**
     * Gets an {@link Optional} of the player
     * @return An {@link Optional} of the player which is empty when the player is offline.
     */
    @NotNull Optional<Player> getPlayer();

}
