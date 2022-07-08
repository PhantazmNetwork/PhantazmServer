package com.github.phantazmnetwork.api.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Attempts to resolve the username of this player. May perform an IO operation to determine the name from the UUID,
     * if necessary, and should cache the results of this operation.
     * @return a {@link CompletableFuture} representing an attempt at retrieving the username of this player. If the
     * username cannot be found due to network conditions or an invalid UUID, the returned String will be the result of
     * calling {@link UUID#toString()} on the stored UUID
     */
    @NotNull CompletableFuture<String> getUsername();

    /**
     * Gets an {@link Optional} which may contain the player, only if they are online. Maintaining strong references to
     * {@link Player} objects should be avoided.
     * @return An {@link Optional} of the player which is empty when the player is offline
     */
    @NotNull Optional<Player> getPlayer();

    /**
     * Gets the {@link Component} display name of the player. May perform an IO operation to determine the player's username.
     * @return The {@link Component} display name of the player
     */
    @NotNull CompletableFuture<Component> getDisplayName();

}
