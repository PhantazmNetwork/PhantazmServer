package org.phantazm.core.guild.party;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.Guild;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.function.Function;

public class Party extends Guild<PartyMember> {

    private final Audience audience;

    private final PartyNotification notification;

    private final MultipleMemberPermission<PartyMember> kickPermission;

    public Party(@NotNull GuildMemberManager<PartyMember> memberManager,
            @NotNull Function<? super PlayerView, ? extends PartyMember> memberCreator, @NotNull Audience audience,
            @NotNull PartyNotification notification,
            @NotNull MultipleMemberPermission<PartyMember> kickPermission) {
        super(memberManager, memberCreator);
        this.audience = Objects.requireNonNull(audience, "audience");
        this.notification = Objects.requireNonNull(notification, "notification");
        this.kickPermission = Objects.requireNonNull(kickPermission, "kickPermission");
    }

    public @NotNull Audience getAudience() {
        return audience;
    }

    public @NotNull PartyNotification getNotification() {
        return notification;
    }

    public @NotNull MultipleMemberPermission<PartyMember> getKickPermission() {
        return kickPermission;
    }
}
