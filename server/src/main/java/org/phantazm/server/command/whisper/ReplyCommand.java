package org.phantazm.server.command.whisper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReplyCommand {

    public static @NotNull Command replyCommand(@NotNull WhisperManager whisperManager) {
        Objects.requireNonNull(whisperManager, "whisperManager");

        Argument<String[]> message = ArgumentType.StringArray("message");
        Command command = new Command("reply", "r");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player || sender instanceof ConsoleSender;
            }

            if (!(sender instanceof Player || sender instanceof ConsoleSender)) {
                sender.sendMessage(Component.text("You have to be a player or the console to use that command!",
                        NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            whisperManager.getLastConverser(sender).ifPresentOrElse(target -> {
                whisperManager.whisper(sender, target, String.join(" ", context.get(message)));
            }, () -> {
                sender.sendMessage(Component.text("Nobody has spoken to you.", NamedTextColor.RED));
            });
        }, message);

        return command;
    }

}
