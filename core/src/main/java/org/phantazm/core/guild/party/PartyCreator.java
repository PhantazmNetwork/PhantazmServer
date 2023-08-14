package org.phantazm.core.guild.party;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.guild.invite.InvitationManager;
import org.phantazm.core.guild.party.notification.PartyNotification;
import org.phantazm.core.guild.party.notification.PartyNotificationConfig;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.guild.permission.RankMultipleMemberPermission;
import org.phantazm.core.guild.permission.RankSingleMemberPermission;
import org.phantazm.core.guild.permission.SingleMemberPermission;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.time.AnalogTickFormatter;
import org.phantazm.core.time.TickFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class PartyCreator {

    private final PartyNotificationConfig notificationConfig;

    private final MiniMessage miniMessage;

    private final TickFormatter tickFormatter;

    private final int creatorRank;

    private final int defaultRank;

    private final long invitationDuration;

    private final int minimumKickRank;

    private final int minimumInviteRank;

    private final int minimumJoinRank;

    public PartyCreator(@NotNull PartyNotificationConfig notificationConfig, @NotNull TickFormatter tickFormatter,
            @NotNull MiniMessage miniMessage, int creatorRank, int defaultRank, long invitationDuration,
            int minimumKickRank, int minimumInviteRank, int minimumJoinRank) {
        this.notificationConfig = Objects.requireNonNull(notificationConfig, "notificationConfig");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        this.creatorRank = creatorRank;
        this.defaultRank = defaultRank;
        this.invitationDuration = invitationDuration;
        this.minimumKickRank = minimumKickRank;
        this.minimumInviteRank = minimumInviteRank;
        this.minimumJoinRank = minimumJoinRank;
    }

    public @NotNull Party createPartyFor(@NotNull PlayerView playerView) {
        Map<UUID, PartyMember> members = new HashMap<>();
        GuildMemberManager<PartyMember> memberManager = new GuildMemberManager<>(members);

        PartyMember owner = new PartyMember(playerView, creatorRank);
        owner.setRank(creatorRank);
        memberManager.addMember(owner);
        Wrapper<PartyMember> ownerWrapper = Wrapper.of(owner);

        Function<? super PlayerView, ? extends PartyMember> memberCreator = this::createMember;
        Audience audience = new PartyAudience(members.values());
        PartyNotification notification =
                new PartyNotification(members.values(), ownerWrapper, notificationConfig, tickFormatter, miniMessage);
        InvitationManager<PartyMember> invitationManager =
                new InvitationManager<>(memberManager, memberCreator, notification, invitationDuration);
        MultipleMemberPermission<PartyMember> kickPermission = new RankMultipleMemberPermission<>(minimumKickRank);
        SingleMemberPermission<PartyMember> invitePermission = new RankSingleMemberPermission<>(minimumInviteRank);
        SingleMemberPermission<PartyMember> joinPermission = new RankSingleMemberPermission<>(minimumJoinRank);

        return new Party(memberManager, memberCreator, audience, notification, invitationManager, kickPermission,
                invitePermission, joinPermission, ownerWrapper);
    }

    private PartyMember createMember(PlayerView playerView) {
        return new PartyMember(playerView, defaultRank);
    }

    public static class Builder {

        private PartyNotificationConfig notificationConfig = PartyNotificationConfig.DEFAULT;

        private TickFormatter tickFormatter = new AnalogTickFormatter(new AnalogTickFormatter.Data(false));

        private MiniMessage miniMessage = MiniMessage.miniMessage();

        private int creatorRank = 1;

        private int defaultRank = 0;

        private long invitationDuration = 1200;

        private int minimumKickRank = 1;

        private int minimumInviteRank = 1;

        private int minimumJoinRank = 1;

        public @NotNull PartyNotificationConfig getNotificationConfig() {
            return notificationConfig;
        }

        public @NotNull Builder setNotificationConfig(@NotNull PartyNotificationConfig notificationConfig) {
            this.notificationConfig = Objects.requireNonNull(notificationConfig, "notificationConfig");
            return this;
        }

        public @NotNull TickFormatter getTickFormatter() {
            return tickFormatter;
        }

        public @NotNull Builder setTickFormatter(@NotNull TickFormatter tickFormatter) {
            this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
            return this;
        }

        public @NotNull MiniMessage getMiniMessage() {
            return miniMessage;
        }

        public @NotNull Builder setMiniMessage(@NotNull MiniMessage miniMessage) {
            this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
            return this;
        }

        public int getCreatorRank() {
            return creatorRank;
        }

        public @NotNull Builder setCreatorRank(int creatorRank) {
            this.creatorRank = creatorRank;
            return this;
        }

        public int getDefaultRank() {
            return defaultRank;
        }

        public @NotNull Builder setDefaultRank(int defaultRank) {
            this.defaultRank = defaultRank;
            return this;
        }

        public long getInvitationDuration() {
            return invitationDuration;
        }

        public @NotNull Builder setInvitationDuration(long invitationDuration) {
            this.invitationDuration = invitationDuration;
            return this;
        }

        public int getMinimumKickRank() {
            return minimumKickRank;
        }

        public @NotNull Builder setMinimumKickRank(int minimumKickRank) {
            this.minimumKickRank = minimumKickRank;
            return this;
        }

        public int getMinimumInviteRank() {
            return minimumInviteRank;
        }

        public @NotNull Builder setMinimumInviteRank(int minimumInviteRank) {
            this.minimumInviteRank = minimumInviteRank;
            return this;
        }

        public int getMinimumJoinRank() {
            return minimumJoinRank;
        }

        public @NotNull Builder setMinimumJoinRank(int minimumJoinRank) {
            this.minimumJoinRank = minimumJoinRank;
            return this;
        }

        public @NotNull PartyCreator build() {
            return new PartyCreator(notificationConfig, tickFormatter, miniMessage, creatorRank, defaultRank,
                    invitationDuration, minimumKickRank, minimumInviteRank, minimumJoinRank);
        }

    }

}
