package com.github.phantazmnetwork.api.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a provider of PlayerView instances, that may be obtained simply from a UUID. The returned PlayerView
 * instances are not necessarily online at the time of creation. PlayerView instances may or may not be cached.
 *
 * @implSpec Implementations should guarantee thread safety.
 */
@FunctionalInterface
public interface PlayerViewProvider {
    /**
     * Returns a {@link PlayerView} instance for the specified UUID.
     * @param uuid the UUID to create the view with
     * @return an PlayerView instance representing a player with the given UUID
     */
    @NotNull PlayerView fromUUID(@NotNull UUID uuid);
}
