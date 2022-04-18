package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Represents a view of a player that may be offline. Provides their UUID as well as a means to access the
 * {@link Player} instance associated with said UUID.
 *
 * @implSpec Implementations should anticipate many calls to {@code getPlayer}, and are encouraged to perform caching
 * if necessary to improve performance. However, since PlayerView instances are intended for long-term storage in fields
 * and other places where they may become out-of-date, it is important to avoid keeping strong references to any cached
 * Player objects within the PlayerView implementation itself.
 * @see BasicPlayerView
 */
public interface PlayerView {

    /**
     * Gets the {@link UUID} of the player.
     * @return The {@link UUID} of the player
     */
    @NotNull UUID getUUID();

    @NotNull CompletableFuture<String> getUsername();

    /**
     * Gets an {@link Optional} which may contain the player, only if they are online.
     * @return An {@link Optional} of the player which is empty when the player is offline
     */
    @NotNull Optional<Player> getPlayer();

}
