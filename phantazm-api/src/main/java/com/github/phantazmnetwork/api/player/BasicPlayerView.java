package com.github.phantazmnetwork.api.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Basic implementation of a {@link PlayerView}. Caches the player object, so {@code getPlayer} is safe to call
 * frequently. Additionally, these objects are safe for long-term storage because they do not store a strong reference
 * to the player.
 *
 * @apiNote This class does not provide an implementation of equals/hashCode because of the inherently volatile nature
 * of its internal state (the cached player reference may be garbage collected at any time). {@code getUUID} should be
 * used if a map-safe, representative object is desired.
 * @see Player
 */
public class BasicPlayerView implements PlayerView {
    private final PlayerContainer playerContainer;

    private final UUID playerUUID;

    private Reference<Player> playerReference;

    private final Object usernameLock = new Object();
    private volatile String username;

    /**
     * Creates a basic {@link PlayerView}.
     * @param playerContainer The {@link PlayerContainer} used to find {@link Player}s based on their {@link UUID}
     * @param playerUUID The {@link UUID} of the {@link Player} to store
     */
    public BasicPlayerView(@NotNull PlayerContainer playerContainer, @NotNull UUID playerUUID) {
        this.playerContainer = Objects.requireNonNull(playerContainer, "playerContainer");
        this.playerUUID = Objects.requireNonNull(playerUUID, "playerUUID");
        this.playerReference = new WeakReference<>(null);
    }

    @Override
    public @NotNull UUID getUUID() {
        return playerUUID;
    }

    @Override
    public @NotNull CompletableFuture<String> getUsername() {
        Player player = playerReference.get();
        if(player != null && player.isOnline()) {
            return CompletableFuture.completedFuture(username = player.getUsername());
        }

        synchronized (usernameLock) {
            if(username != null) {
                return CompletableFuture.completedFuture(username);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            JsonObject response = MojangUtils.fromUuid(playerUUID.toString());
            if(response != null) {
                JsonElement nameElement = response.get(FieldNames.NAME);
                if(nameElement != null && nameElement.isJsonPrimitive()) {
                    JsonPrimitive primitive = nameElement.getAsJsonPrimitive();
                    if(primitive.isString()) {
                        synchronized (usernameLock) {
                            return username = primitive.getAsString();
                        }
                    }
                }
            }

            return playerUUID.toString();
        });
    }

    @Override
    public @NotNull Optional<Player> getPlayer() {
        //first, try to get the cached player
        Player player = playerReference.get();
        if(player != null && player.isOnline()) {
            return Optional.of(player);
        }

        //if null or offline, update the reference (may still be null)
        player = playerContainer.getPlayer(playerUUID);
        playerReference = new WeakReference<>(player);
        return Optional.ofNullable(player);
    }

}
