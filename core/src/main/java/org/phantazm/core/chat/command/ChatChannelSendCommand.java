package org.phantazm.core.chat.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatChannel;

import java.util.Objects;

public class ChatChannelSendCommand {

    private ChatChannelSendCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command chatChannelSend(@NotNull String commandName, @NotNull ChatChannel channel) {
        Objects.requireNonNull(channel, "channel");
        Command command = new Command(commandName);

        Argument<String[]> message = ArgumentType.StringArray("message");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player)sender;
            channel.findAudience(player.getUuid(), audience -> {
                channel.formatMessage(player, String.join(" ", context.get(message)))
                        .whenComplete((component, throwable) -> {
                            if (component == null) {
                                return;
                            }

                            audience.sendMessage(component);
                        });
            }, (failure) -> {
                player.sendMessage(failure.left());
            });
        }, message);

        return command;
    }

}
