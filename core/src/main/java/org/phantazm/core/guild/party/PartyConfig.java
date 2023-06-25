package org.phantazm.core.guild.party;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.command.PartyCommandConfig;
import org.phantazm.core.guild.party.notification.PartyNotificationConfig;

public record PartyConfig(@NotNull PartyNotificationConfig notificationConfig,
                          @NotNull PartyCommandConfig commandConfig, int creatorRank, int defaultRank, long invitationDuration, int minimumKickRank, int minimumInviteRank,
                          int minimumJoinRank) {
}
