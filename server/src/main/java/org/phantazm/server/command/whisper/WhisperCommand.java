package org.phantazm.server.command.whisper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WhisperCommand {

    public static @NotNull Command whisperCommand(@NotNull ConnectionManager connectionManager,
            @NotNull WhisperManager whisperManager) {
        Objects.requireNonNull(connectionManager, "connectionManager");
        Objects.requireNonNull(whisperManager, "whisperManager");

        Command command = new Command("whisper", "w", "msg");
        Argument<String> target = ArgumentType.Word("target");
        Argument<String[]> message = ArgumentType.StringArray("message");

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
            String name = context.get(target);
            Player targetPlayer = connectionManager.getPlayer(name);

            if (targetPlayer == null) {
                sender.sendMessage(Component.text(name + " is not online."));
                return;
            }

            whisperManager.whisper(sender, targetPlayer, String.join(" ", context.get(message)));
        }, target, message);

        return command;
    }

}
