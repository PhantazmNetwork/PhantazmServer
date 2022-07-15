package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * {@link Command} used to switch between chat channels.
 */
public class ChatCommand extends Command {

    /**
     * The ID for this command.
     */
    public static final String COMMAND_ID = "chat";

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Creates a chat {@link Command}.
     *
     * @param channels                   A {@link Map} of {@link String} channel names to {@link ChatChannel}s.
     * @param playerChannels             A {@link Map} of player channel names based on their {@link UUID}
     * @param defaultChannelNameSupplier A {@link Supplier} of the default {@link ChatChannel} name
     */
    public ChatCommand(@NotNull Map<String, ChatChannel> channels, @NotNull Map<UUID, String> playerChannels,
                       @NotNull Supplier<String> defaultChannelNameSupplier) {
        super(COMMAND_ID);

        Objects.requireNonNull(channels, "channels");
        Objects.requireNonNull(playerChannels, "playerChannels");
        Objects.requireNonNull(defaultChannelNameSupplier, "defaultChannelNameSupplier");

        String[] channelNames = channels.keySet().toArray(EMPTY_STRING_ARRAY);
        Argument<String> channelNameArgument = new ArgumentWord("channel").from(channelNames);
        channelNameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (String channelName : channelNames) {
                suggestion.addEntry(new SuggestionEntry(channelName));
            }
        });

        channelNameArgument.setCallback((sender, exception) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid channel! Valid options are ");

            for (int i = 0; i < channelNames.length; i++) {
                String name = channelNames[i];
                stringBuilder.append(name);

                if (i < channelNames.length - 2) {
                    stringBuilder.append(", ");
                }
                else if (i == channelNames.length - 2) {
                    if (channelNames.length != 2) {
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

            sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
            return false;
        }, (sender, context) -> {
            Player player = (Player)sender;

            String channelName = context.get(channelNameArgument);
            Component channelNameComponent = Component.text(channelName, NamedTextColor.GOLD);
            String previousChannelName =
                    playerChannels.computeIfAbsent(player.getUuid(), uuid -> defaultChannelNameSupplier.get());

            JoinConfiguration joinConfiguration = JoinConfiguration.separator(Component.space());
            if (channelName.equals(previousChannelName)) {
                Component message = Component.join(joinConfiguration, Component.text("You are already in the"),
                                                   channelNameComponent, Component.text("channel!")
                ).color(NamedTextColor.RED);
                sender.sendMessage(message);
                return;
            }

            playerChannels.put(player.getUuid(), channelName);
            Component message = Component.textOfChildren(Component.text("Set chat channel to "), channelNameComponent,
                                                         Component.text(".")
            ).color(NamedTextColor.GREEN);
            player.sendMessage(message);
        }, channelNameArgument);
    }

}
