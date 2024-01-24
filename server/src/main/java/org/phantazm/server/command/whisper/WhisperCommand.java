package org.phantazm.server.command.whisper;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;

import java.util.Objects;

public class WhisperCommand {

    public static @NotNull Command whisperCommand(@NotNull ConnectionManager connectionManager,
        @NotNull WhisperManager whisperManager) {
        Objects.requireNonNull(connectionManager);
        Objects.requireNonNull(whisperManager);

        Command command = new Command("whisper", "w", "msg");
        Argument<String> target = ArgumentType.Word("target");
        target.setSuggestionCallback((sender, context, suggestion) -> {
            String prefix = context.getOrDefault(target, "").trim().toLowerCase();

            for (Player player : connectionManager.getOnlinePlayers()) {
                if (player == sender) {
                    continue;
                }

                String username = player.getUsername();
                if (username.toLowerCase().startsWith(prefix)) {
                    suggestion.addEntry(new SuggestionEntry(username));
                }
            }
        });
        Argument<String[]> message = ArgumentType.StringArray("message");

        command.addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
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
