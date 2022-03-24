package com.github.phantazmnetwork.api.channel;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

/**
 * Settings for an {@link Audience}'s connected channels.
 */
public interface ConnectedChannelSettings {

    /**
     * The {@link ChatChannel} to which an {@link Audience}'s messages should be sent to.
     * @return The {@link ChatChannel}
     */
    @NotNull ChatChannel getMessageChannel();

}
