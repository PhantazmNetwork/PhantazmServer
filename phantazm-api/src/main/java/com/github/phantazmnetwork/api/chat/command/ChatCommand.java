package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;


import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link Command} used to switch between chat channels.
 */
public class ChatCommand extends Command {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Creates a chat {@link Command}.
     * @param channels A map of channels based on a {@link String} key
     * @param playerChannels A map of player channels based on their {@link UUID}
     * @param chatChannelChangeCallback A callback for when a {@link Player}'s channel changes
     */
    public ChatCommand(@NotNull Map<String, ChatChannel> channels,
                       @NotNull Map<UUID, ChatChannel> playerChannels,
                       @NotNull ChatChannelChangeCallback chatChannelChangeCallback) {
        super("chat");

        Objects.requireNonNull(playerChannels, "playerChannels");
        Objects.requireNonNull(chatChannelChangeCallback, "channelChangeCallback");

        String[] channelNames = channels.keySet().toArray(EMPTY_STRING_ARRAY);
        Argument<String> channelArgument = ArgumentType.Word("channel").from(channelNames);
        channelArgument.setCallback((sender, exception) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid Channel! Valid options are ");
            for (int i = 0; i < channelNames.length - 1; i++) {
                stringBuilder.append(channelNames[i]).append(", ");
            }
            if (channelNames.length > 0) {
                stringBuilder.append(channelNames[channelNames.length - 1]).append(".");
            }
            sender.sendMessage(Component.text(stringBuilder.toString(), NamedTextColor.RED));
        });

        addConditionalSyntax((sender, commandString) -> {
            if (sender instanceof Player) {
                return true;
            }

            sender.sendMessage(Component.text("You have to be a player to use that command!",
                    NamedTextColor.RED));
            return false;
        }, (sender, context) -> {
            Player player = (Player) sender;
            String channelName = context.get(channelArgument);
            ChatChannel channel = channels.get(channelName);
            ChatChannel previousChannel = playerChannels.get(player.getUuid());
            if (channel == previousChannel) {
                sender.sendMessage(Component.text("You are already in the " + channelName.toUpperCase() +
                        " channel!", NamedTextColor.RED));
                return;
            }

            chatChannelChangeCallback.onChannelChanged(player, channels.get(channelName));
            player.sendMessage(Component.text()
                    .append(Component.text("Set chat channel to "),
                            Component.text(channelName.toUpperCase(), NamedTextColor.GOLD),
                            Component.text("."))
                    .color(NamedTextColor.GREEN));
        }, channelArgument);
    }

}
