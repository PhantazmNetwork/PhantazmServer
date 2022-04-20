package com.github.phantazmnetwork.api.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@FunctionalInterface
public interface ChatChannel {

    /**
     * Broadcasts a {@link Component}.
     * @param sender The associated {@link CommandSender}, or null if none such exists
     * @param message The message to send
     * @param messageType The type of the message
     * @param filter A filter for {@link Audience}s in the channel
     */
    void broadcast(@Nullable CommandSender sender, @NotNull Component message, @NotNull MessageType messageType,
                   @NotNull Predicate<? super Audience> filter);

}
