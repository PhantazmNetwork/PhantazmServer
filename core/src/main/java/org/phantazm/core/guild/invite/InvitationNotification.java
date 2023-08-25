package org.phantazm.core.guild.invite;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.player.PlayerView;

public interface InvitationNotification<TMember extends GuildMember> {

    void notifyJoin(@NotNull TMember member);

    void notifyInvitation(@NotNull TMember inviter, @NotNull PlayerView invitee, long invitationDuration);

    void notifyExpiry(@NotNull PlayerView inviter, @NotNull PlayerView invitee);

}
