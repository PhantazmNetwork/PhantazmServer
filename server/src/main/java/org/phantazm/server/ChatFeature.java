package org.phantazm.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatChannel;
import org.phantazm.core.chat.ChatConfig;
import org.phantazm.core.chat.InstanceChatChannel;
import org.phantazm.core.chat.command.ChatChannelSendCommand;
import org.phantazm.core.chat.command.ChatCommand;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyChatChannel;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.server.role.RoleStore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Entrypoint for chat-related features.
 */
public final class ChatFeature {
    /**
     * The default {@link ChatChannel} name for players.
     */
    public static final String DEFAULT_CHAT_CHANNEL_NAME = "all";

    private ChatFeature() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes chat features. Should only be called once from {@link PhantazmServer#main(String[])}.
     *
     * @param viewProvider A {@link PlayerViewProvider} to create {@link PlayerView}s
     * @param chatConfig   Chat settings
     * @param parties      A map of player {@link UUID}s to {@link Party} instances
     */
    static void initialize(@NotNull PlayerViewProvider viewProvider, @NotNull ChatConfig chatConfig,
            @NotNull Map<? super UUID, ? extends Party> parties, @NotNull RoleStore roleStore) {
        Map<String, ChatChannel> channels = new HashMap<>() {
            @Override
            public boolean remove(Object key, Object value) {
                if (key.equals(DEFAULT_CHAT_CHANNEL_NAME)) {
                    throw new IllegalArgumentException("Cannot remove default channel");
                }

                return super.remove(key, value);
            }
        };

        Function<? super Player, ? extends Component> nameFormatter = (player) -> {
            return roleStore.getStylingRole(player).styleName(player);
        };

        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();
        CommandManager commandManager = MinecraftServer.getCommandManager();
        ChatChannel defaultChannel = new InstanceChatChannel(viewProvider, MiniMessage.miniMessage(),
                chatConfig.chatFormats().get(DEFAULT_CHAT_CHANNEL_NAME), nameFormatter);
        channels.put(DEFAULT_CHAT_CHANNEL_NAME, defaultChannel);
        commandManager.register(ChatChannelSendCommand.chatChannelSend("ac", defaultChannel));
        ChatChannel partyChannel = new PartyChatChannel(parties, viewProvider, MiniMessage.miniMessage(),
                chatConfig.chatFormats().get(PartyChatChannel.CHANNEL_NAME), nameFormatter);
        channels.put(PartyChatChannel.CHANNEL_NAME, partyChannel);
        commandManager.register(ChatChannelSendCommand.chatChannelSend("pc", partyChannel));

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
                Component message = channel.formatMessage(event.getPlayer(), event.getMessage());
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
