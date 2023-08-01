package org.phantazm.core.friend.command;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record FriendCommandConfig(@NotNull Component mustBeAPlayer,
                                  @NotNull String addTargetNotOnlineFormat,
                                  @NotNull String alreadyFriendsFormat,
                                  @NotNull Component mustBeFriends,
                                  @NotNull Component alreadyRequested) {
}
