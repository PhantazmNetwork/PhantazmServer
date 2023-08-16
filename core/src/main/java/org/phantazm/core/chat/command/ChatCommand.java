package org.phantazm.core.chat.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatChannel;

import java.util.Iterator;
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

    /**
     * Creates a chat {@link Command}.
     *
     * @param channels                   A {@link Map} of {@link String} channel names to {@link ChatChannel}s.
     * @param playerChannels             A {@link Map} of player channel names based on their {@link UUID}
     * @param defaultChannelNameSupplier A {@link Supplier} of the default {@link ChatChannel} name
     */
    public ChatCommand(@NotNull Map<String, ChatChannel> channels, @NotNull Map<UUID, String> playerChannels,
            @NotNull Map<String, String> aliasResolver,
            @NotNull Supplier<String> defaultChannelNameSupplier) {
        super(COMMAND_ID, "ch");

        Objects.requireNonNull(channels, "channels");
        Objects.requireNonNull(playerChannels, "playerChannels");
        Objects.requireNonNull(aliasResolver, "aliasResolver");
        Objects.requireNonNull(defaultChannelNameSupplier, "defaultChannelNameSupplier");

        Argument<String> channelNameArgument = ArgumentType.String("channel");
        channelNameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (String channelName : channels.keySet()) {
                suggestion.addEntry(new SuggestionEntry(channelName));
            }
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
            String previousChannelName =
                    playerChannels.computeIfAbsent(player.getUuid(), uuid -> defaultChannelNameSupplier.get());

            if (!aliasResolver.containsKey(channelName)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid channel! Valid options are ");

                Iterator<String> channelIterator = channels.keySet().iterator();
                for (int i = 0; channelIterator.hasNext(); ++i) {
                    String name = channelIterator.next();
                    stringBuilder.append(name);

                    if (i < channels.size() - 2) {
                        stringBuilder.append(", ");
                    }
                    else if (i == channels.size() - 2) {
                        if (channels.size() != 2) {
                            stringBuilder.append(",");
                        }
                        stringBuilder.append(" or ");
                    }
                }
                stringBuilder.append(".");

                sender.sendMessage(Component.text(stringBuilder.toString(), NamedTextColor.RED));
                return;
            }

            channelName = aliasResolver.get(channelName);
            Component channelNameComponent = Component.text(channelName, NamedTextColor.GOLD);
            if (channelName.equals(previousChannelName)) {
                Component message = Component.text()
                        .append(Component.text("You are already in the "), channelNameComponent,
                                Component.text(" channel!")).color(NamedTextColor.RED).build();
                sender.sendMessage(message);
                return;
            }

            playerChannels.put(player.getUuid(), channelName);
            Component message = Component.text()
                    .append(Component.text("Set chat channel to "), channelNameComponent, Component.text("."))
                    .color(NamedTextColor.GREEN).build();
            player.sendMessage(message);
        }, channelNameArgument);
    }

}
