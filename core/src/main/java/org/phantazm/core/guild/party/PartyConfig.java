package org.phantazm.core.guild.party;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.command.PartyCommandConfig;
import org.phantazm.core.guild.party.notification.PartyNotificationConfig;

public record PartyConfig(
    @NotNull PartyNotificationConfig notificationConfig,
    @NotNull PartyCommandConfig commandConfig,
    @NotNull String spyChatFormat,
    int creatorRank,
    int defaultRank,
    long invitationDuration,
    int minimumKickRank,
    int minimumInviteRank,
    int minimumJoinRank,
    int minimumAllInviteRank) {

    public static final PartyConfig DEFAULT =
        new PartyConfig(PartyNotificationConfig.DEFAULT, PartyCommandConfig.DEFAULT, "", 1, 0,
            60L * MinecraftServer.TICK_PER_SECOND, 1, 1, 1, 1);

}
