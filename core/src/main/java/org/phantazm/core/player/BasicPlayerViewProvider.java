package org.phantazm.core.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PlayerViewProvider} based off of a {@link ConnectionManager}, which will be the ConnectionManager instance
 * used by all {@link PlayerView}s created by this class. PlayerView instances are cached.
 *
 * @apiNote It is expected that clients will create only one instance of this class, following a singleton pattern.
 * There should only be a single active PlayerView instance corresponding to any given UUID at any time.
 */
public class BasicPlayerViewProvider implements PlayerViewProvider {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(2);

    private final IdentitySource identitySource;
    private final ConnectionManager connectionManager;

    //used only for the purposes of maintaining strong references to players who are online
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Player, PlayerViewImpl> onlinePlayers;

    private final Cache<UUID, PlayerViewImpl> uuidToView;
    private final Cache<String, UUID> nameToUuid;

    /**
     * Creates a new BasicPlayerViewProvider instance from the given parameters.
     *
     * @param identitySource    the {@link IdentitySource} used to resolve names, if necessary
     * @param connectionManager the {@link ConnectionManager} used by this server
     * @param duration          the duration for which name-to-UUID mappings will be cached
     */
    public BasicPlayerViewProvider(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
        @NotNull Duration duration) {
        this.identitySource = Objects.requireNonNull(identitySource);
        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.onlinePlayers = new ConcurrentHashMap<>();

        this.uuidToView = Caffeine.newBuilder().weakValues().build();
        this.nameToUuid = Caffeine.newBuilder().expireAfterWrite(Objects.requireNonNull(duration)).build();
    }

    /**
     * Creates a new BasicPlayerViewProvider instance from the given parameters and the default duration of two minutes
     * for name-to-UUID mapping invalidation.
     *
     * @param identitySource    the {@link IdentitySource} used to resolve names, if necessary
     * @param connectionManager the {@link ConnectionManager} used by this server
     */
    public BasicPlayerViewProvider(@NotNull IdentitySource identitySource,
        @NotNull ConnectionManager connectionManager) {
        this(identitySource, connectionManager, DEFAULT_TIMEOUT);
    }

    @Override
    public @NotNull PlayerView fromUUID(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);
        return uuidToView.get(uuid, key -> new PlayerViewImpl(identitySource, connectionManager, key));
    }

    @Override
    public @NotNull CompletableFuture<Optional<PlayerView>> fromName(@NotNull String name) {
        Objects.requireNonNull(name);
        Player player = connectionManager.getPlayer(name);
        if (player != null) {
            //if player is online, use the player object
            nameToUuid.put(name, player.getUuid());
            return CompletableFuture.completedFuture(Optional.of(fromPlayer(player)));
        }

        //if the player is offline, check the nameToUuid cache
        UUID cachedUuid = nameToUuid.getIfPresent(name);
        if (cachedUuid != null) {
            //we were able to resolve the name, so return a corresponding PlayerView
            return CompletableFuture.completedFuture(Optional.of(fromUUID(cachedUuid)));
        }

        //if we have no UUID cached for this player, try to resolve a UUID using the IdentitySource
        return identitySource.getUUID(name).thenApply(uuidOptional -> uuidOptional.map(uuid -> {
            nameToUuid.put(name, uuid);
            return fromUUID(uuid);
        }));
    }

    @Override
    public @NotNull Optional<PlayerView> fromNameIfOnline(@NotNull String name) {
        Objects.requireNonNull(name);
        Player player = connectionManager.getPlayer(name);
        if (player == null) {
            return Optional.empty();
        }

        return Optional.of(fromPlayer(player));
    }

    @Override
    public @NotNull PlayerView fromPlayer(@NotNull Player player) {
        Objects.requireNonNull(player);
        return uuidToView.get(player.getUuid(), key -> new PlayerViewImpl(identitySource, connectionManager, player));
    }

    /**
     * Adds a strong reference to a {@link PlayerViewImpl} associated with the given {@link Player}. If there is already
     * a weakly or strongly referenced PlayerViewImpl associated with this player's UUID, that instance is used.
     * Otherwise, a new instance is created and cached.
     *
     * @param player the player to add
     */
    @ApiStatus.Internal
    void addPlayer(@NotNull Player player) {
        onlinePlayers.put(player, uuidToView.get(player.getUuid(), key ->
            new PlayerViewImpl(identitySource, connectionManager, player)));
    }

    /**
     * Removes a strong reference to an associated {@link PlayerViewImpl}.
     *
     * @param player the player to remove an association with
     */
    @ApiStatus.Internal
    void removePlayer(@NotNull Player player) {
        onlinePlayers.remove(player);
    }
}
