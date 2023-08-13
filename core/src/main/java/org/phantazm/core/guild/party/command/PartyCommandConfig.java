package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record PartyCommandConfig(@NotNull Component notInParty,
                                 @NotNull Component alreadyInParty,
                                 @NotNull Component mustBeAPlayer,
                                 @NotNull Component mustBeOwner,
                                 @NotNull String playerNotOnlineFormat,
                                 @NotNull String cannotFindPlayerFormat,
                                 @NotNull Component createCommandSuccess,
                                 @NotNull Component cannotInvitePlayers,
                                 @NotNull Component automaticPartyCreation,
                                 @NotNull Component cannotInviteSelf,
                                 @NotNull String inviteeAlreadyInPartyFormat,
                                 @NotNull String inviteeAlreadyInvitedFormat,
                                 @NotNull String toJoinNotInParty,
                                 @NotNull Component noInvite,
                                 @NotNull Component cannotKickMembers,
                                 @NotNull String toKickNotInPartyFormat,
                                 @NotNull Component cannotKickSelf,
                                 @NotNull String cannotKickOtherFormat,
                                 @NotNull String listFormat,
                                 @NotNull String onlineMemberFormat,
                                 @NotNull String offlineMemberFormat,
                                 @NotNull String toTransferNotInPartyFormat,
                                 @NotNull Component cannotTransferToSelf) {

    public static final PartyCommandConfig DEFAULT =
            new PartyCommandConfig(Component.empty(), Component.empty(), Component.empty(), Component.empty(), "", "",
                    Component.empty(), Component.empty(), Component.empty(), Component.empty(), "", "", "",
                    Component.empty(), Component.empty(), "", Component.empty(), "", "", "", "", "", Component.empty());

}
