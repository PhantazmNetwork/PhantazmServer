package org.phantazm.core.friend.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.friend.FriendCache;

import java.util.Objects;

public class FriendListCommand {

    private FriendListCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command listCommand(@NotNull FriendCommandConfig config, @NotNull FriendCache cache) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(cache);

        Command command = new Command("list");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player)sender;
            return;
        });

        return command;
    }

}
