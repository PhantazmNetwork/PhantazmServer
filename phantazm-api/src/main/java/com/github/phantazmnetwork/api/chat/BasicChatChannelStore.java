package com.github.phantazmnetwork.api.chat;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of a {@link ChatChannelStore}.
 */
public class BasicChatChannelStore implements ChatChannelStore {

    private final Map<String, ChatChannel> channels = new HashMap<>();

    private final String defaultChannelName;

    private final ChatChannel defaultChannel;

    /**
     * Creates a basic {@link ChatChannelStore}.
     *
     * @param defaultChannelName The name of the default {@link ChatChannel}
     * @param defaultChannel The default {@link ChatChannel}
     */
    public BasicChatChannelStore(@NotNull String defaultChannelName, @NotNull ChatChannel defaultChannel) {
        this.defaultChannelName = Objects.requireNonNull(defaultChannelName, "defaultChannelName");
        this.defaultChannel = Objects.requireNonNull(defaultChannel, "defaultChannel");
        channels.put(defaultChannelName, defaultChannel);
    }

    @Override
    public @NotNull ChatChannel getDefaultChannel() {
        return defaultChannel;
    }

    @Override
    public void registerChannel(@NotNull String name, @NotNull ChatChannel channel) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(channel, "channel");

        if (channels.containsKey(name)) {
            throw new IllegalArgumentException("channel already registered");
        }

        channels.put(name, channel);
    }

    @Override
    public void unregisterChannel(@NotNull String name) {
        Objects.requireNonNull(name, "name");

        if (defaultChannelName.equals(name)) {
            throw new IllegalArgumentException("Cannot unregister default channel");
        }
        if (channels.remove(name) == null) {
            throw new IllegalArgumentException("Cannot not registered");
        }
    }

    @Override
    public @NotNull Map<String, ChatChannel> getChannels() {
        return Collections.unmodifiableMap(channels);
    }

}
