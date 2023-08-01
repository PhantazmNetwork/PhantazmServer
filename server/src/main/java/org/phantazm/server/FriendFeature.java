package org.phantazm.server;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.friend.*;
import org.phantazm.core.friend.command.FriendCommand;
import org.phantazm.core.player.PlayerViewProvider;

public class FriendFeature {

    static void initialize(@NotNull FriendConfig config, @NotNull CommandManager commandManager,
            @NotNull ConnectionManager connectionManager, @NotNull SchedulerManager schedulerManager,
            @NotNull PlayerViewProvider viewProvider) {
        FriendDatabase database = new SQLFriendDatabase(HikariFeature.getDataSource(), ExecutorFeature.getExecutor());
        FriendNotification notification =
                new FriendNotification(config.notificationConfig(), MiniMessage.miniMessage());
        FriendRequestManager requestManager = new FriendRequestManager(database, notification, 60 * 20L);

        Command command =
                FriendCommand.friendCommand(config.commandConfig(), MiniMessage.miniMessage(), connectionManager,
                        database, notification, requestManager, viewProvider);
        commandManager.register(command);

        schedulerManager.scheduleTask(() -> {
            requestManager.tick(System.currentTimeMillis());
        }, TaskSchedule.immediate(), TaskSchedule.nextTick());
    }

}
