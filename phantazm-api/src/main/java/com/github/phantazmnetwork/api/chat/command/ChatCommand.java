package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import com.github.phantazmnetwork.api.chat.ChatChannelStore;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;


import java.util.*;

/**
 * {@link Command} used to switch between chat channels.
 */
public class ChatCommand extends Command {

    /**
     * Creates a chat {@link Command}.
     *
     * @param channelStore A {@link ChatChannelStore} for the command to use
     * @param playerChannels A map of player channels based on their {@link UUID}
     */
    public ChatCommand(@NotNull ChatChannelStore channelStore, @NotNull Map<UUID, ChatChannel> playerChannels) {
        super("chat");

        Objects.requireNonNull(playerChannels, "playerChannels");

        Argument<Pair<String, ChatChannel>> channelArgument = new ArgumentChannel("channel", channelStore);
        channelArgument.setCallback((sender, exception) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid channel! Valid options are ");

            Collection<String> channelNames = channelStore.getChannels().keySet();
            int i = 0;
            for (Iterator<String> iterator = channelNames.iterator(); iterator.hasNext(); i++) {
                String name = iterator.next();
                stringBuilder.append(name);

                if (i < channelNames.size() - 2) {
                    stringBuilder.append(", ");
                }
                else if (i == channelNames.size() - 2) {
                    if (channelNames.size() != 2) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(" or ");
                }
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
            Pair<String, ChatChannel> channel = context.get(channelArgument);
            ChatChannel previousChannel = playerChannels.get(player.getUuid());
            if (channel.right() == previousChannel) {
                sender.sendMessage(Component.text("You are already in the " + channel.left().toUpperCase() +
                        " channel!", NamedTextColor.RED));
                return;
            }

            playerChannels.put(player.getUuid(), channel.right());
            player.sendMessage(Component.text()
                    .append(Component.text("Set chat channel to "),
                            Component.text(channel.left().toUpperCase(), NamedTextColor.GOLD),
                            Component.text("."))
                    .color(NamedTextColor.GREEN));
        }, channelArgument);
    }

}
