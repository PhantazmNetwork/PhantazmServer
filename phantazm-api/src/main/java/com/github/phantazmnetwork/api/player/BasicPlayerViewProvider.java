package com.github.phantazmnetwork.api.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * A {@link PlayerViewProvider} based off of a {@link PlayerContainer}, which will be the PlayerContainer instance used
 * by all {@link PlayerView}s created by this class. PlayerView instances are cached.
 *
 * @apiNote It is expected that clients will create only one instance of this class, following a singleton pattern.
 * There should only be a single active PlayerView instance corresponding to any given UUID at any time.
 */
public class BasicPlayerViewProvider implements PlayerViewProvider {
    private final PlayerContainer container;
    private final Cache<UUID, PlayerView> viewCache;

    /**
     * Creates a new {@link BasicPlayerViewProvider}, using the provided container.
     * @param container the container this instance will use to create {@link PlayerView} instances
     */
    public BasicPlayerViewProvider(@NotNull PlayerContainer container) {
        this.container = Objects.requireNonNull(container, "container");
        this.viewCache = Caffeine.newBuilder().weakValues().build();
    }

    @Override
    public @NotNull PlayerView create(@NotNull UUID uuid) {
        //caching *might* be unnecessary here, but could save memory
        return viewCache.get(uuid, key -> new BasicPlayerView(container, key));
    }
}
