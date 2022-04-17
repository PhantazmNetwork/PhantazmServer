package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
    public @NotNull Optional<Player> getPlayer() {
        //first, try to get the cached player
        Player player = playerReference.get();
        if(player != null) {

            //only return the cached player if they're online
            if(player.isOnline()) {
                return Optional.of(player);
            }
        }

        //if null or offline, update the reference (may still be null)
        player = playerContainer.getPlayer(playerUUID);
        playerReference = new WeakReference<>(player);
        return Optional.ofNullable(player);
    }

}
