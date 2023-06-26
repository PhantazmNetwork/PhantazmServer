package org.phantazm.core.guild.party.notification;

import org.jetbrains.annotations.NotNull;

public record PartyNotificationConfig(
        @NotNull String joinToPartyFormat,
        @NotNull String joinToJoinerFormat,
        @NotNull String inviteToPartyFormat,
        @NotNull String inviteToInviteeFromOwnerFormat,
        @NotNull String inviteToInviteeFromOtherFormat,
        @NotNull String expiryToPartyFormat,
        @NotNull String expiryToInviteeFormat,
        @NotNull String leaveToPartyFormat,
        @NotNull String leaveToLeaverFormat,
        @NotNull String kickToPartyFormat,
        @NotNull String kickToKickedFormat,
        @NotNull String transferFormat
) {
}
