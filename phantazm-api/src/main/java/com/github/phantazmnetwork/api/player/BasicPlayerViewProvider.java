package com.github.phantazmnetwork.api.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.mojang.MojangUtils;
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

    private final ConnectionManager connectionManager;
    private final Cache<UUID, PlayerView> uuidToView;
    private final Cache<String, UUID> nameToUuid;

    /**
     * Creates a new BasicPlayerViewProvider from the specified {@link ConnectionManager} and timeout duration, which is
     * used to determine when to invalidate any given name-to-UUID mapping maintained by this class. These mappings are
     * used by {@link BasicPlayerViewProvider#fromName(String)} to avoid making unnecessary requests to the Mojang API.
     * They are not used by {@link BasicPlayerViewProvider#fromNameIfOnline(String)}.
     *
     * @param connectionManager the ConnectionManager instance to use for retrieving players
     * @param duration the duration after writing the cache will consider entries to have expired
     */
    public BasicPlayerViewProvider(@NotNull ConnectionManager connectionManager, @NotNull Duration duration) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.uuidToView = Caffeine.newBuilder().weakValues().build();
        this.nameToUuid = Caffeine.newBuilder().expireAfterWrite(Objects.requireNonNull(duration, "duration"))
                .build();
    }

    /**
     * Creates a new BasicPlayerViewProvider using the default timeout duration — name to UUID mappings will be
     * invalidated 2 minutes from creation.
     * @param connectionManager the ConnectionManager instance to use for retrieving players
     */
    public BasicPlayerViewProvider(@NotNull ConnectionManager connectionManager) {
        this(connectionManager, DEFAULT_TIMEOUT);
    }

    @Override
    public @NotNull PlayerView fromUUID(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return uuidToView.get(uuid, key -> new BasicPlayerView(connectionManager, key));
    }

    @Override
    public @NotNull CompletableFuture<PlayerView> fromName(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        Player player = connectionManager.getPlayer(name);
        if(player != null) {
            //if player is online, use the player object
            nameToUuid.put(name, player.getUuid());
            return CompletableFuture.completedFuture(fromPlayer(player));
        }

        //if the player is offline, check the nameToUuid cache
        UUID uuid = nameToUuid.getIfPresent(name);
        if(uuid != null) {
            //we were able to resolve the name, so return a corresponding PlayerView
            return CompletableFuture.completedFuture(fromUUID(uuid));
        }

        return CompletableFuture.supplyAsync(() -> {
            //make a request to mojang for the UUID, returns null if no such name exists
            JsonObject object = MojangUtils.fromUsername(name);
            if(object != null) {
                JsonElement id = object.get(MojangJSONKeys.PLAYER_ID);
                if(id != null && id.isJsonPrimitive()) {
                    JsonPrimitive idPrimitive = id.getAsJsonPrimitive();
                    if(idPrimitive.isString()) {
                        UUID requestUuid = UUID.fromString(idPrimitive.getAsString());
                        nameToUuid.put(name, requestUuid);
                        return fromUUID(requestUuid);
                    }
                }
            }

            return null;
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
        return uuidToView.get(player.getUuid(), key -> new BasicPlayerView(connectionManager, player));
    }
}
