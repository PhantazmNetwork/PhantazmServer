package com.github.phantazmnetwork.api.chat.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.phantazmnetwork.api.chat.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * {@link Command} used to switch between chat channels.
 */
public class ChatCommand extends Command {

    /**
     * The ID for this command.
     */
    public static final String COMMAND_ID = "chat";

    /**
     * Creates a chat {@link Command}.
     *
     * @param channelFinders A map of channel names to a {@link Function}
     *                       that will get a {@link ChatChannel} for a specific {@link Player}
     * @param playerChannels A cache of player channels based on their {@link UUID}
     * @param defaultChannel The default channel to be used
     */
    public ChatCommand(@NotNull Map<String, Function<Player, ChatChannel>> channelFinders,
                       @NotNull Cache<UUID, ChatChannel> playerChannels, @NotNull ChatChannel defaultChannel) {
        super(COMMAND_ID);

        Objects.requireNonNull(channelFinders, "channelFinders");
        Objects.requireNonNull(playerChannels, "playerChannels");

        Argument<Function<Player, ChatChannel>> channelArgument = new ArgumentChannel("channel", channelFinders);
        channelArgument.setCallback((sender, exception) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid channel! Valid options are ");

            Collection<String> channelNames = channelFinders.keySet();
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
        channelArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (String name : channelFinders.keySet()) {
                suggestion.addEntry(new SuggestionEntry(name));
            }
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
            String channelName = context.getRaw(channelArgument).toUpperCase();

            ChatChannel channel = context.get(channelArgument).apply(player);
            if (channel == null) {
                sender.sendMessage(Component.text("You can't join the " + channelName + " channel!",
                        NamedTextColor.RED));
                return;
            }


            ChatChannel previousChannel = playerChannels.get(player.getUuid(), (unused) -> defaultChannel);
            if (channel == previousChannel) {
                sender.sendMessage(Component.text("You are already in the " + channelName + " channel!",
                        NamedTextColor.RED));
                return;
            }

            playerChannels.put(player.getUuid(), channel);
            player.sendMessage(Component
                    .textOfChildren(
                            Component.text("Set chat channel to "),
                            Component.text(channelName, NamedTextColor.GOLD),
                            Component.text(".")
                    ).color(NamedTextColor.GREEN));
        }, channelArgument);
    }

}
