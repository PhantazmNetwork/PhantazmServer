package org.phantazm.core.friend.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.friend.FriendDatabase;
import org.phantazm.core.friend.FriendNotification;
import org.phantazm.core.friend.FriendRequestManager;
import org.phantazm.core.player.PlayerViewProvider;

public class FriendCommand {

    private FriendCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command friendCommand(@NotNull FriendCommandConfig config, @NotNull MiniMessage miniMessage,
            @NotNull ConnectionManager connectionManager, @NotNull FriendDatabase database,
            @NotNull FriendNotification notification, @NotNull FriendRequestManager requestManager,
            @NotNull PlayerViewProvider viewProvider) {
        Command command = new Command("friend", "f");

        command.addSubcommand(
                FriendAddCommand.addCommand(config, miniMessage, connectionManager, database, requestManager,
                        viewProvider));
        command.addSubcommand(
                FriendRemoveCommand.removeCommand(config, miniMessage, connectionManager, database, notification,
                        viewProvider));

        return command;
    }

}
