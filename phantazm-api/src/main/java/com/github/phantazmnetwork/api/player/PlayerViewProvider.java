package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a provider of PlayerView instances, that may be obtained from a UUID, name, or Player instance. The
 * returned PlayerView instances are not necessarily online at the time of creation. PlayerView instances may or may not
 * be cached.
 *
 * @implSpec Implementations should guarantee thread safety.
 */
public interface PlayerViewProvider {
    /**
     * Returns a {@link PlayerView} instance for the specified UUID.
     * @param uuid the UUID to create the view with
     * @return an PlayerView instance representing a player with the given UUID
     */
    @NotNull PlayerView fromUUID(@NotNull UUID uuid);

    /**
     * <p>Resolves the given name and attempts to create a corresponding {@link PlayerView} instance. Since this
     * requires determining the UUID of the player, it may be necessary to make a request through Mojang's API, which
     * will be performed asynchronously. Implementations should perform caching as-necessary to avoid making too many
     * of these requests.</p>
     *
     * <p>The returned {@link CompletableFuture} will provide a PlayerView instance upon completion. This may be null
     * if a request was performed and failed due to network conditions or an invalid argument (like a nonexistent name).
     * The Player represented by the PlayerView need not be online.</p>
     *
     * @param name the name of the player to resolve
     * @return a CompletableFuture instance containing the view, or null if the name is invalid or a network problem
     * occurred
     * @see PlayerViewProvider#fromNameIfOnline(String)
     */
    @NotNull CompletableFuture<PlayerView> fromName(@NotNull String name);

    /**
     * Optionally returns a {@link PlayerView} instance with the given name, if they exist and are online. However, note
     * that it is not guaranteed that the player represented by the PlayerView will still be online when this method
     * returns.
     * @param name the name of the player
     * @return an Optional that will only contain a corresponding PlayerView if a player with the specified name exists
     * and is online
     * @see PlayerViewProvider#fromName(String)
     */
    @NotNull Optional<PlayerView> fromNameIfOnline(@NotNull String name);

    /**
     * Creates a {@link PlayerView} from an already-existing {@link Player}.
     * @param player the player to create a PlayerView instance from
     * @return a PlayerView instance for the given Player
     */
    @NotNull PlayerView fromPlayer(@NotNull Player player);
}
