package com.github.phantazmnetwork.api.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a chat channel. This is backed by an {@link Audience}, where messages are sent to.
 */
public interface ChatChannel {

    /**
     * Finds an {@link Audience} to send messages to. This represents the "channel".
     * @param channelMember The player that is a member of this channel
     * @param onSuccess A callback when an {@link Audience} is successfully found
     * @param onFailure A callback when an {@link Audience} is not found that provides an error {@link Component} message
     *                  that is meant to be sent to the channel member
     */
    void findAudience(@NotNull UUID channelMember, @NotNull Consumer<Audience> onSuccess,
                      @NotNull Consumer<Component> onFailure);

    /**
     * Formats a message. Channels may add custom style or formatting.
     * @param chatEvent The {@link PlayerChatEvent} that is being formatted
     * @return The formatted {@link Component} message
     */
    @NotNull Component formatMessage(@NotNull PlayerChatEvent chatEvent);

}
