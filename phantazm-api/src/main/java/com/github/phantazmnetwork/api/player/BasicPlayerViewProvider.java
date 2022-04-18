package com.github.phantazmnetwork.api.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A {@link PlayerViewProvider} based off of a {@link PlayerContainer}, which will be the PlayerContainer instance used
 * by all {@link PlayerView}s created by this class. PlayerView instances are cached.
 *
 * @apiNote It is expected that clients will create only one instance of this class, following a singleton pattern.
 * There should only be a single active PlayerView instance corresponding to any given UUID at any time.
 */
public class BasicPlayerViewProvider implements PlayerViewProvider {
    private final PlayerContainer container;
    private final Cache<UUID, PlayerView> uuidToView;
    private final Cache<String, UUID> nameToUuid;

    /**
     * Creates a new BasicPlayerViewProvider from the specified container and timeout information, which is used to
     * determine when to invalidate any given name-to-UUID mapping maintained by this class. These mappings are used by
     * {@link BasicPlayerViewProvider#fromName(String)} to avoid making unnecessary requests to the Mojang API. They are
     * not used by {@link BasicPlayerViewProvider#fromNameIfOnline(String)}. An entry is expired
     *
     * @param container the container to use for retrieving players
     * @param nameCacheTimeout the number of units after writing the cache will consider entries to have expired
     * @param timeoutUnits the type of units used for measuring timeout
     */
    public BasicPlayerViewProvider(@NotNull PlayerContainer container, long nameCacheTimeout,
                                   @NotNull TimeUnit timeoutUnits) {
        this.container = Objects.requireNonNull(container, "container");
        this.uuidToView = Caffeine.newBuilder().weakValues().build();
        this.nameToUuid = Caffeine.newBuilder().expireAfterWrite(nameCacheTimeout, timeoutUnits).build();
    }

    /**
     * Creates a new BasicPlayerViewProvider using the basic timeout information — name to UUID mappings will be
     * invalidated 2 minutes from creation.
     * @param container the container to use for retrieving players
     */
    public BasicPlayerViewProvider(@NotNull PlayerContainer container) {
        this(container, 2, TimeUnit.MINUTES);
    }

    private PlayerView fromUUIDInternal(UUID uuid) {
        return uuidToView.get(uuid, key -> new BasicPlayerView(container, key));
    }

    @Override
    public @NotNull PlayerView fromUUID(@NotNull UUID uuid) {
        return fromUUIDInternal(uuid);
    }

    @Override
    public @NotNull CompletableFuture<PlayerView> fromName(@NotNull String name) {
        Player player = container.getPlayer(name);
        UUID uuid;
        if(player != null) {
            //if player is online, use the UUID from the player object
            uuid = player.getUuid();
            nameToUuid.put(name, uuid);
        }
        else {
            //if the player is offline, check the nameToUuid cache
            uuid = nameToUuid.getIfPresent(name);
        }

        if(uuid != null) {
            //we were able to resolve the name, so return a corresponding PlayerView
            return CompletableFuture.completedFuture(fromUUIDInternal(uuid));
        }

        return CompletableFuture.supplyAsync(() -> {
            //make a request to mojang for the UUID, returns null if no such name exists
            JsonObject object = MojangUtils.fromUsername(name);
            if(object != null) {
                JsonElement id = object.get(FieldNames.ID);
                if(id != null && id.isJsonPrimitive()) {
                    JsonPrimitive idPrimitive = id.getAsJsonPrimitive();
                    if(idPrimitive.isString()) {
                        UUID requestUuid = UUID.fromString(idPrimitive.getAsString());
                        nameToUuid.put(name, requestUuid);
                        return fromUUIDInternal(requestUuid);
                    }
                }
            }

            return null;
        });
    }

    @Override
    public @NotNull Optional<PlayerView> fromNameIfOnline(@NotNull String name) {
        Player player = container.getPlayer(name);
        if(player == null) {
            return Optional.empty();
        }

        UUID uuid = player.getUuid();
        return Optional.of(uuidToView.get(uuid, key -> new BasicPlayerView(container, uuid)));
    }
}
