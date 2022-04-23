package com.github.phantazmnetwork.api.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private final Cache<UUID, PlayerView> uuidToView;
    private final Cache<String, UUID> nameToUuid;

    public BasicPlayerViewProvider(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
                                   @NotNull Duration duration) {
        this.identitySource = Objects.requireNonNull(identitySource, "identitySource");
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.uuidToView = Caffeine.newBuilder().weakValues().build();
        this.nameToUuid = Caffeine.newBuilder().expireAfterWrite(Objects.requireNonNull(duration, "duration"))
                .build();
    }

    public BasicPlayerViewProvider(@NotNull IdentitySource identitySource,
                                   @NotNull ConnectionManager connectionManager) {
        this(identitySource, connectionManager, DEFAULT_TIMEOUT);
    }

    @Override
    public @NotNull PlayerView fromUUID(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return uuidToView.get(uuid, key -> new BasicPlayerView(identitySource, connectionManager, key));
    }

    @Override
    public @NotNull CompletableFuture<Optional<PlayerView>> fromName(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        Player player = connectionManager.getPlayer(name);
        if(player != null) {
            //if player is online, use the player object
            nameToUuid.put(name, player.getUuid());
            return CompletableFuture.completedFuture(Optional.of(fromPlayer(player)));
        }

        //if the player is offline, check the nameToUuid cache
        UUID cachedUuid = nameToUuid.getIfPresent(name);
        if(cachedUuid != null) {
            //we were able to resolve the name, so return a corresponding PlayerView
            return CompletableFuture.completedFuture(Optional.of(fromUUID(cachedUuid)));
        }

        return CompletableFuture.supplyAsync(() -> {
            Optional<UUID> uuidOptional = identitySource.getUUID(name);
            if(uuidOptional.isPresent()) {
                UUID uuid = uuidOptional.get();
                nameToUuid.put(name, uuid);
                return Optional.of(fromUUID(uuid));
            }

            return Optional.empty();
        });
    }

    @Override
    public @NotNull Optional<PlayerView> fromNameIfOnline(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        Player player = connectionManager.getPlayer(name);
        if(player == null) {
            return Optional.empty();
        }

        return Optional.of(fromPlayer(player));
    }

    @Override
    public @NotNull PlayerView fromPlayer(@NotNull Player player) {
        Objects.requireNonNull(player, "player");
        return uuidToView.get(player.getUuid(), key -> new BasicPlayerView(identitySource, connectionManager, player));
    }
}
