package org.phantazm.core.chat.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatChannel;
import org.phantazm.core.command.CommandUtils;

import java.util.Objects;

public class ChatChannelSendCommand {

    private ChatChannelSendCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command chatChannelSend(@NotNull String commandName, @NotNull ChatChannel channel) {
        Objects.requireNonNull(channel);
        Command command = new Command(commandName);

        Argument<String[]> message = ArgumentType.StringArray("message");
        command.addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player) sender;
            channel.sendMessage(player, String.join(" ", context.get(message)), failure -> {
                player.sendMessage(failure.left());
            });
        }, message);

        return command;
    }

}
