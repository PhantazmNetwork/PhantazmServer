package org.phantazm.core.chat;

import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents a chat channel. This is backed by an {@link Audience}, where messages are sent to.
 */
public interface ChatChannel {

    /**
     * Finds an {@link Audience} to send messages to. This represents the "channel".
     *
     * @param channelMember The player that is a member of this channel
     * @param onSuccess     A callback when an {@link Audience} is successfully found
     * @param onFailure     A callback when an {@link Audience} is not found that provides an error {@link Component} message
     *                      that is meant to be sent to the channel member and whether the chat channel should be returned to the default
     */
    void findAudience(@NotNull UUID channelMember, @NotNull Consumer<Audience> onSuccess,
            @NotNull Consumer<ObjectBooleanPair<Component>> onFailure);

    /**
     * Formats a message. Channels may add custom style or formatting.
     *
     * @return The formatted {@link Component} message
     */
    @NotNull CompletableFuture<Component> formatMessage(@NotNull Player player, @NotNull String message);

}
