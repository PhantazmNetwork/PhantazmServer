package com.github.phantazmnetwork.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.api.chat.ChatChannel;
import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.api.chat.command.ChatCommand;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

final class ChatInitializer {
    private ChatInitializer() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> eventNode) {
        ChatChannel defaultChannel = (sender, message, messageType, filter) -> {
            if (sender instanceof Entity entity && sender instanceof Identified identified) {
                Instance instance = entity.getInstance();
                if (instance != null) {
                    instance.filterAudience(filter).sendMessage(identified, Component.text().append(Component
                            .text("all")).append(message), messageType);
                }
            }
        };

        ChatChannel selfChannel = (sender, message, messageType, filter) -> {
            if (sender != null) {
                Identity identity = (sender instanceof Identified identified)
                        ? identified.identity()
                        : Identity.nil();
                sender.filterAudience(filter).sendMessage(identity, Component.text().append(Component
                        .text("self")).append(message), messageType);
            }
        };

        Map<String, Function<Player, ChatChannel>> channels = new HashMap<>() {
            @Override
            public boolean remove(Object key, Object value) {
                if (key.equals(PhantazmServer.DEFAULT_CHAT_CHANNEL_NAME)) {
                    throw new IllegalArgumentException("Cannot remove default channel");
                }

                return super.remove(key, value);
            }
        };

        channels.put(PhantazmServer.DEFAULT_CHAT_CHANNEL_NAME, (unused) -> defaultChannel);
        channels.put("self", (unused) -> selfChannel);

        Cache<UUID, ChatChannel> playerChannels = Caffeine.newBuilder().weakValues().build();
        MinecraftServer.getCommandManager().register(new ChatCommand(channels, playerChannels, defaultChannel));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, event -> {
            event.setCancelled(true);

            ChatChannel channel = playerChannels.get(event.getPlayer().getUuid(), (unused) -> defaultChannel);
            Component message = (event.getChatFormatFunction() != null)
                    ? event.getChatFormatFunction().apply(event)
                    : event.getDefaultChatFormat().get();

            ChatChannelSendEvent channelSendEvent = new ChatChannelSendEvent(channel, event.getPlayer(),
                    event.getMessage(), message);
            eventNode.callCancellable(channelSendEvent,
                    () -> channel.broadcast(event.getPlayer(), channelSendEvent.getMessage(), MessageType.CHAT,
                            audience -> true));
        });
    }
}
