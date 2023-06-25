package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record PartyCommandConfig(@NotNull Component notInParty,
                                 @NotNull Component alreadyInParty,
                                 @NotNull Component mustBeAPlayer,
                                 @NotNull String playerNotOnlineFormat,
                                 @NotNull String cannotFindPlayerFormat,
                                 @NotNull Component createCommandSuccess,
                                 @NotNull Component cannotInvitePlayers,
                                 @NotNull Component automaticPartyCreation,
                                 @NotNull Component cannotInviteSelf,
                                 @NotNull String inviteeAlreadyInPartyFormat,
                                 @NotNull String toJoinNotInParty,
                                 @NotNull Component noInvite,
                                 @NotNull Component cannotKickMembers,
                                 @NotNull String toKickNotInPartyFormat,
                                 @NotNull Component cannotKickSelf,
                                 @NotNull String cannotKickOtherFormat) {
}
