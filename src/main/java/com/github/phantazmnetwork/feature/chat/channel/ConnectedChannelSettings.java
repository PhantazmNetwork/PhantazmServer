package com.github.phantazmnetwork.feature.chat.channel;

import org.jetbrains.annotations.NotNull;

/**
 * Settings for an {@link net.kyori.adventure.audience.Audience}'s connected channels
 */
public interface ConnectedChannelSettings {

    /**
     * The {@link ChatChannel} to which an {@link net.kyori.adventure.audience.Audience}'s messages should be sent to
     * @return The {@link ChatChannel}
     */
    @NotNull ChatChannel messageChannel();

}
