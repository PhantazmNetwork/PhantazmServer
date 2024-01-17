package org.phantazm.core.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PlayerViewProvider} based off of a {@link ConnectionManager}, which will be the ConnectionManager instance
 * used by all {@link PlayerView}s created by this class. PlayerView instances are cached, and are removed from the
 * cache when going offline.
 *
 * @apiNote It is expected that clients will create only one instance of this class, following a singleton pattern.
 * There should only be a single active PlayerView instance corresponding to any given UUID at any time.
 */
public class BasicPlayerViewProvider implements PlayerViewProvider {
    private final IdentitySource identitySource;
    private final ConnectionManager connectionManager;

    //only used to ensure PlayerViewImpl instances aren't GC'd
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<UUID, PlayerViewImpl> strongReferences;

    private final Cache<UUID, PlayerViewImpl> uuidToView;

    /**
     * Creates a new BasicPlayerViewProvider instance from the given parameters.
     *
     * @param identitySource    the {@link IdentitySource} used to resolve names, if necessary
     * @param connectionManager the {@link ConnectionManager} used by this server
     */
    public BasicPlayerViewProvider(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager) {
        this.identitySource = Objects.requireNonNull(identitySource);
        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.strongReferences = new ConcurrentHashMap<>();
        this.uuidToView = Caffeine.newBuilder().weakValues().build();
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
            return CompletableFuture.completedFuture(Optional.of(fromPlayer(player)));
        }

        //if we have no UUID cached for this player, try to resolve a UUID using the IdentitySource
        return identitySource.getUUID(name).thenApply(uuidOptional -> uuidOptional.map(this::fromUUID));
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
        return uuidToView.get(player.getUuid(), key -> new PlayerViewImpl(identitySource, connectionManager, key));
    }

    @Override
    public void handleJoin(@NotNull Player player) {
        Objects.requireNonNull(player);
        uuidToView.get(player.getUuid(), key -> {
            PlayerViewImpl newPlayer = new PlayerViewImpl(identitySource, connectionManager, player);
            strongReferences.put(key, newPlayer);
            return newPlayer;
        });
    }

    @Override
    public void handleDisconnect(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);
        strongReferences.remove(uuid);
    }
}
