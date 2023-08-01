package org.phantazm.core.friend;

import org.jetbrains.annotations.NotNull;

public record FriendNotificationConfig(@NotNull String requestToRequesterFormat,
                                       @NotNull String requestToTargetFormat,
                                       @NotNull String acceptToRequesterFormat,
                                       @NotNull String acceptToTargetFormat,
                                       @NotNull String expiryToRequesterFormat,
                                       @NotNull String expiryToTargetFormat,
                                       @NotNull String removeToRemoverFormat,
                                       @NotNull String removeToTargetFormat) {
}
