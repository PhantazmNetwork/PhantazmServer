package org.phantazm.core.friend;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.friend.command.FriendCommandConfig;

public record FriendConfig(@NotNull FriendCommandConfig commandConfig,
                           @NotNull FriendNotificationConfig notificationConfig) {
}
