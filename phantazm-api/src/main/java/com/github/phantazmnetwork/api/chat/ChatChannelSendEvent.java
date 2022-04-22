package com.github.phantazmnetwork.api.chat;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Event for when a {@link Player} broadcasts a message in a {@link ChatChannel}.
 */
public class ChatChannelSendEvent implements PlayerEvent, CancellableEvent {

    private final ChatChannel channel;

    private final Player sender;

    private final String input;

    private Component message;

    private boolean cancel;

    /**
     * Creates a new chat channel send event.
     *
     * @param channel The {@link ChatChannel} the message will be sent to
     * @param sender The sender of the message
     * @param input The actual message the sender sent
     * @param message The rendered message that will be sent to the {@link ChatChannel}
     */
    public ChatChannelSendEvent(@NotNull ChatChannel channel, @NotNull Player sender, @NotNull String input,
                                @NotNull Component message) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.sender = Objects.requireNonNull(sender, "sender");
        this.input = Objects.requireNonNull(input, "input");
        this.message = Objects.requireNonNull(message, "message");
    }

    /**
     * Gets the {@link ChatChannel} to which the message will be sent.
     *
     * @return The {@link ChatChannel} to which the message will be sent
     */
    public @NotNull ChatChannel getChannel() {
        return channel;
    }

    /**
     * Gets the {@link Player} that sent the message.
     *
     * @return The {@link Player} that sent the message
     */
    public @NotNull Player getPlayer() {
        return sender;
    }

    /**
     * Gets the message that will be sent to the {@link ChatChannel}.
     *
     * @return The message that will be sent to the {@link ChatChannel}
     */
    public @NotNull Component getMessage() {
        return message;
    }

    /**
     * Gets the actual message that the sender sent.
     *
     * @return The actual message that the sender sent
     */
    public @NotNull String getInput() {
        return input;
    }

    /**
     * Sets the message that will be sent to the {@link ChatChannel}.
     *
     * @param message The new message
     */
    public void setMessage(@NotNull Component message) {
        this.message = Objects.requireNonNull(message, "message");
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

}
