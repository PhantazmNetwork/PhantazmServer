package org.phantazm.core.guild.party;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.guild.invite.InvitationManager;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.guild.permission.RankMultipleMemberPermission;
import org.phantazm.core.guild.permission.RankSingleMemberPermission;
import org.phantazm.core.guild.permission.SingleMemberPermission;
import org.phantazm.core.player.PlayerView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PartyCreator {

    private final int creatorRank;

    private final int defaultRank;

    private final long invitationDuration;

    private final int minimumKickRank;

    private final int minimumInviteRank;

    public PartyCreator(int creatorRank, int defaultRank, long invitationDuration, int minimumKickRank,
            int minimumInviteRank) {
        this.creatorRank = creatorRank;
        this.defaultRank = defaultRank;
        this.invitationDuration = invitationDuration;
        this.minimumKickRank = minimumKickRank;
        this.minimumInviteRank = minimumInviteRank;
    }

    public @NotNull Party createPartyFor(@NotNull PlayerView playerView) {
        Map<? super UUID, PartyMember> members = new HashMap<>();
        GuildMemberManager<PartyMember> memberManager = new GuildMemberManager<>(members);

        PartyMember owner = new PartyMember(playerView, creatorRank);
        owner.setRank(creatorRank);
        memberManager.addMember(owner);
        Wrapper<PartyMember> ownerWrapper = Wrapper.of(owner);

        Function<? super PlayerView, ? extends PartyMember> memberCreator = this::createMember;
        Audience audience = new PartyAudience(members.values());
        PartyNotification notification = new PartyNotification(members.values(), ownerWrapper);
        InvitationManager<PartyMember> invitationManager =
                new InvitationManager<>(memberManager, memberCreator, notification, invitationDuration);
        MultipleMemberPermission<PartyMember> kickPermission = new RankMultipleMemberPermission<>(minimumKickRank);
        SingleMemberPermission<PartyMember> invitePermission = new RankSingleMemberPermission<>(minimumInviteRank);

        return new Party(memberManager, memberCreator, audience, notification, invitationManager, kickPermission,
                invitePermission, ownerWrapper);
    }

    private PartyMember createMember(PlayerView playerView) {
        return new PartyMember(playerView, defaultRank);
    }

}
