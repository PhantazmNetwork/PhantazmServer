package org.phantazm.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatChannel;
import org.phantazm.core.chat.InstanceChatChannel;
import org.phantazm.core.chat.SelfChatChannel;
import org.phantazm.core.chat.command.ChatCommand;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entrypoint for chat-related features.
 */
public final class Chat {
    /**
     * The default {@link ChatChannel} name for players.
     */
    public static final String DEFAULT_CHAT_CHANNEL_NAME = "all";

    private Chat() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes chat features. Should only be called once from {@link PhantazmServer#main(String[])}.
     *
     * @param node           the node to register chat-related events to
     * @param viewProvider   A {@link PlayerViewProvider} to create {@link PlayerView}s
     * @param commandManager The {@link CommandManager} to register chat commands to
     */
    static void initialize(@NotNull EventNode<Event> node, @NotNull PlayerViewProvider viewProvider,
            @NotNull CommandManager commandManager) {
        Map<String, ChatChannel> channels = new HashMap<>() {
            @Override
            public boolean remove(Object key, Object value) {
                if (key.equals(DEFAULT_CHAT_CHANNEL_NAME)) {
                    throw new IllegalArgumentException("Cannot remove default channel");
                }

                return super.remove(key, value);
            }
        };

        channels.put(DEFAULT_CHAT_CHANNEL_NAME, new InstanceChatChannel(viewProvider));
        channels.put("self", new SelfChatChannel(viewProvider));

        Map<UUID, String> playerChannels = new HashMap<>();
        commandManager.register(new ChatCommand(channels, playerChannels, () -> DEFAULT_CHAT_CHANNEL_NAME));
        node.addListener(PlayerLoginEvent.class, event -> {
            UUID uuid = event.getPlayer().getUuid();
            playerChannels.putIfAbsent(uuid, DEFAULT_CHAT_CHANNEL_NAME);
        });
        node.addListener(PlayerChatEvent.class, event -> {
            event.setCancelled(true);

            Player player = event.getPlayer();
            UUID uuid = player.getUuid();
            String channelName = playerChannels.putIfAbsent(uuid, DEFAULT_CHAT_CHANNEL_NAME);
            ChatChannel channel = channels.get(channelName);
            if (channel == null) {
                return;
            }

            channel.findAudience(uuid, audience -> {
                Component message = channel.formatMessage(event);
                audience.sendMessage(message);
            }, failure -> {
                player.sendMessage(failure.left());
                if (failure.rightBoolean()) {
                    player.sendMessage(Component.text().append(Component.text("Set channel to "),
                                    Component.text(DEFAULT_CHAT_CHANNEL_NAME, NamedTextColor.GOLD), Component.text("."))
                            .color(NamedTextColor.GREEN));
                    playerChannels.put(player.getUuid(), DEFAULT_CHAT_CHANNEL_NAME);
                }
            });
        });
    }
}
