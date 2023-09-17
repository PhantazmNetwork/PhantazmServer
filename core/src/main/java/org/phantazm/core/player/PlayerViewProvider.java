package org.phantazm.core.player;

import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a provider of PlayerView instances, that may be obtained from a UUID, name, or Player instance. The
 * returned PlayerView instances are not necessarily online at the time of creation. PlayerView instances may or may not
 * be cached.
 *
 * @implSpec Implementations should guarantee thread safety for all methods.
 */
public interface PlayerViewProvider {
    class Global {
        private static final Object INITIALIZATION_LOCK = new Object();
        private static PlayerViewProvider instance;

        public static void init(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
            @NotNull Duration duration) {
            synchronized (INITIALIZATION_LOCK) {
                if (instance != null) {
                    throw new IllegalArgumentException("PlayerViewProvider has already been initialized");
                }

                Global.instance = new BasicPlayerViewProvider(identitySource, connectionManager, duration);
            }
        }

        public static @NotNull PlayerViewProvider instance() {
            PlayerViewProvider instance = Global.instance;
            if (instance == null) {
                throw new IllegalArgumentException("PlayerViewProvider has not yet been initialized");
            }

            return instance;
        }
    }

    /**
     * Returns a {@link PlayerView} instance for the specified UUID.
     *
     * @param uuid the UUID to create the view with
     * @return an PlayerView instance representing a player with the given UUID
     */
    @NotNull PlayerView fromUUID(@NotNull UUID uuid);

    /**
     * <p>Resolves the given name and attempts to create a corresponding {@link PlayerView} instance. Since this
     * requires determining the UUID of the player, it may be necessary to perform an IO operation, which will be done
     * asynchronously. Implementations should perform caching of results as-necessary.</p>
     *
     * <p>The returned {@link CompletableFuture} will provide a PlayerView instance upon completion. This may be null
     * if a request was performed and failed due to IO error or an invalid argument (like a nonexistent name). The
     * Player represented by the PlayerView need not be online.</p>
     *
     * <p>In general, if an online player exists with the provided name, no IO will be performed.</p>
     *
     * @param name the name of the player to resolve
     * @return a CompletableFuture instance of an Optional containing the PlayerView, which will be empty if the player
     * is offline, does not exist, or is offline but there was an IO error when attempting to retrieve a UUID from an
     * {@link IdentitySource}
     * @see PlayerViewProvider#fromNameIfOnline(String)
     */
    @NotNull CompletableFuture<Optional<PlayerView>> fromName(@NotNull String name);

    /**
     * Optionally returns a {@link PlayerView} instance with the given name, if they exist and are online. However, note
     * that it is not guaranteed that the player represented by the PlayerView will still be online when this method
     * returns.
     *
     * @param name the name of the player
     * @return an Optional that will only contain a corresponding PlayerView if a player with the specified name exists
     * and is online
     * @see PlayerViewProvider#fromName(String)
     */
    @NotNull Optional<PlayerView> fromNameIfOnline(@NotNull String name);

    /**
     * Creates a {@link PlayerView} from an already-existing {@link Player}.
     *
     * @param player the player to create a PlayerView instance from
     * @return a PlayerView instance for the given Player
     */
    @NotNull PlayerView fromPlayer(@NotNull Player player);
}
