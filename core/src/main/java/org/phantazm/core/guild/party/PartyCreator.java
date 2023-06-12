package org.phantazm.core.guild.party;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.guild.permission.RankMultipleMemberPermission;
import org.phantazm.core.player.PlayerView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PartyCreator {

    private final int minimumKickRank;

    private final int creatorRank;

    private final int defaultRank;

    public PartyCreator(int minimumKickRank, int creatorRank, int defaultRank) {
        this.minimumKickRank = minimumKickRank;
        this.creatorRank = creatorRank;
        this.defaultRank = defaultRank;
    }

    public @NotNull Party createPartyFor(@NotNull PlayerView playerView) {
        Map<? super UUID, PartyMember> members = new HashMap<>();
        GuildMemberManager<PartyMember> memberManager = new GuildMemberManager<>(members);
        Function<? super PlayerView, ? extends PartyMember> partyCreator = this::createMember;
        Audience audience = new PartyAudience(members.values());
        PartyNotification notification = new PartyNotification(members.values());
        MultipleMemberPermission<PartyMember> kickPermission = new RankMultipleMemberPermission<>(minimumKickRank);
        Party party = new Party(memberManager, partyCreator, audience, notification, kickPermission);

        PartyMember owner = new PartyMember(playerView, creatorRank);
        owner.setRank(creatorRank);
        memberManager.addMember(owner);

        return party;
    }

    private PartyMember createMember(PlayerView playerView) {
        return new PartyMember(playerView, defaultRank);
    }

}
