package org.phantazm.core.guild.invite;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.function.Function;

public class InvitationManager<TMember extends GuildMember> implements Tickable {

    private final Object2LongMap<UUID> latestInviteTime = new Object2LongArrayMap<>();

    private final Queue<Invitation> invitations = new LinkedList<>();

    private final GuildMemberManager<TMember> memberManager;

    private final Function<? super PlayerView, ? extends TMember> playerCreator;

    private final InvitationNotification<TMember> notification;

    private final long invitationDuration;

    private long ticks = 0;

    public InvitationManager(@NotNull GuildMemberManager<TMember> memberManager,
            @NotNull Function<? super PlayerView, ? extends TMember> playerCreator,
            @NotNull InvitationNotification<TMember> notification, long invitationDuration) {
        this.memberManager = Objects.requireNonNull(memberManager, "memberManager");
        this.playerCreator = Objects.requireNonNull(playerCreator, "playerCreator");
        this.notification = Objects.requireNonNull(notification, "notification");
        this.invitationDuration = invitationDuration;
    }

    @Override
    public void tick(long time) {
        ++ticks;

        for (Invitation invitation = invitations.poll(); invitation != null; invitation = invitations.poll()) {
            if (invitation.expirationTime() > ticks) {
                break;
            }

            if (latestInviteTime.getLong(invitation.invitee().getUUID()) == invitation.expirationTime()) {
                latestInviteTime.removeLong(invitation.invitee().getUUID());
            }
            notification.notifyExpiry(invitation.invitee());
        }
    }

    public void invite(@NotNull TMember inviter, @NotNull PlayerView invitee) {
        long expirationTime = ticks + invitationDuration;
        invitations.add(new Invitation(invitee, expirationTime));
        latestInviteTime.put(invitee.getUUID(), expirationTime);

        notification.notifyInvitation(inviter, invitee);
    }

    public boolean hasInvitation(@NotNull UUID candidate) {
        return latestInviteTime.containsKey(candidate);
    }

    public void acceptInvitation(@NotNull PlayerView invitee) {
        Iterator<Invitation> iterator = invitations.iterator();
        boolean anyInvites = false;
        while (iterator.hasNext()) {
            Invitation next = iterator.next();
            if (next.invitee().getUUID().equals(invitee.getUUID())) {
                iterator.remove();
                anyInvites = true;
            }
        }

        if (!anyInvites) {
            throw new IllegalStateException("Player was not invited");
        }

        latestInviteTime.removeLong(invitee.getUUID());
        TMember newMember = playerCreator.apply(invitee);
        memberManager.addMember(newMember);
        notification.notifyJoin(newMember);
    }

    private record Invitation(@NotNull PlayerView invitee, long expirationTime) {

    }

}