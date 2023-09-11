package org.phantazm.core.player;

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
 * @implSpec Implementations should anticipate many calls to {@code getPlayer}, and are encouraged to perform caching if
 * necessary to improve performance. However, since PlayerView instances are intended for long-term storage in fields
 * and other places where they may become out-of-date, it is important to avoid keeping strong references to any cached
 * Player objects within the PlayerView implementation itself.
 * <p>
 * Implementations must also ensure an {@link Object#equals(Object)} and {@link Object#hashCode()} implementation based
 * only on equality checking or hashing the {@link UUID} returned by calling {@link PlayerView#getUUID()}.
 * @see PlayerViewImpl
 */
public sealed interface PlayerView permits PlayerViewImpl {

    /**
     * Gets the {@link UUID} of the player.
     *
     * @return The {@link UUID} of the player
     */
    @NotNull UUID getUUID();

    /**
     * Attempts to resolve the username of this player. May perform an IO operation to determine the name from the UUID,
     * if necessary, and should cache the results of this operation.
     *
     * @return a {@link CompletableFuture} representing an attempt at retrieving the username of this player. If the
     * username cannot be found due to network conditions or an invalid UUID, the returned String will be the result of
     * calling {@link UUID#toString()} on the stored UUID
     */
    @NotNull CompletableFuture<String> getUsername();

    /**
     * Retrieves the player's username immediately if it is cached in this object, or the player is currently online.
     * May be out-of-date, as it is not defined when (if ever) a previously cached username becomes invalid.
     *
     * @return an Optional containing the cached username, or an empty Optional if not present
     */
    @NotNull Optional<String> getUsernameIfCached();

    /**
     * Asynchronously gets the display name of this player. If they are currently online, this function will immediately
     * exit with a completed {@link CompletableFuture} containing the result of calling {@link Player#getDisplayName()}.
     * Otherwise, the player's username will be resolved given their UUID (which may entail a request to Mojang's API
     * servers) and a plain text component (with no styling applied) containing the player's username will, when the
     * operation completes, be set as the future's value.
     *
     * @return a CompletableFuture containing the player's current display name
     */
    @NotNull CompletableFuture<Component> getDisplayName();

    /**
     * Immediately gets the display name of this player if it is cached in this object, or if the player is currently
     * online. May be out-of-date, as it is not defined when (if ever) a previously cached display name becomes
     * invalid.
     *
     * @return an Optional containing the cached username, or an empty Optional if not present
     */
    @NotNull Optional<Component> getDisplayNameIfCached();

    /**
     * Gets an {@link Optional} which may contain the player, only if they are online. Maintaining strong references to
     * {@link Player} objects should be avoided.
     *
     * @return An {@link Optional} of the player which is empty when the player is offline
     */
    @NotNull Optional<Player> getPlayer();
}
