package com.github.phantazmnetwork.api.chat;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Store for {@link ChatChannel}s.
 */
public interface ChatChannelStore {

    /**
     * Gets the default {@link ChatChannel} used for the store.
     *
     * @return The default {@link ChatChannel}
     */
    @NotNull ChatChannel getDefaultChannel();

    /**
     * Registers a {@link ChatChannel}.
     *
     * @param name The name of the {@link ChatChannel}
     * @param channel The {@link ChatChannel} to register
     * @throws IllegalArgumentException If a {@link ChatChannel} is already registered with the given name
     */
    void registerChannel(@NotNull String name, @NotNull ChatChannel channel);

    /**
     * Unregisters a {@link ChatChannel}.
     *
     * @param name The name of the {@link ChatChannel}
     * @throws IllegalArgumentException If no {@link ChatChannel}s are already registered with the given name
     */
    void unregisterChannel(@NotNull String name);

    /**
     * Gets a copy of the store's {@link ChatChannel}s.
     *
     * @return The store's {@link ChatChannel}s
     */
    @NotNull Map<String, ChatChannel> getChannels();

}
