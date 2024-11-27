package org.phantazm.server.command.whisper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.CommandUtils;

import java.util.Objects;

public class WhisperCommand {

    public static @NotNull Command whisperCommand(@NotNull ConnectionManager connectionManager,
        @NotNull WhisperManager whisperManager) {
        Objects.requireNonNull(connectionManager);
        Objects.requireNonNull(whisperManager);

        Command command = new Command("whisper", "w", "msg");
        Argument<String> target = ArgumentType.Word("target");
        target.setSuggestionCallback((sender, context, suggestion) -> {
            CommandUtils.tabComplete(suggestion, connectionManager.getOnlinePlayers(), onlinePlayer -> {
                if (onlinePlayer == sender) return null;

                return onlinePlayer.getUsername();
            }, Player::getUsername, Player::getDisplayName);
        });

        Argument<String[]> message = ArgumentType.StringArray("message");
        command.addConditionalSyntax(CommandUtils.PLAYER_CONDITION, (sender, context) -> {
            String name = context.get(target);
            Player targetPlayer = connectionManager.getPlayer(name);

            if (targetPlayer == null) {
                sender.sendMessage(Component.text(name + " is not online!", NamedTextColor.RED));
                return;
            }

            whisperManager.whisper(sender, targetPlayer, String.join(" ", context.get(message)));
        }, target, message);

        return command;
    }

}
