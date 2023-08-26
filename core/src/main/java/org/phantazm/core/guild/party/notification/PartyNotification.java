package org.phantazm.core.guild.party.notification;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.invite.InvitationNotification;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.time.TickFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PartyNotification implements InvitationNotification<PartyMember> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyNotification.class);

    private final Audience audience;

    private final Wrapper<PartyMember> owner;

    private final PartyNotificationConfig config;

    private final TickFormatter tickFormatter;

    private final MiniMessage miniMessage;

    public PartyNotification(@NotNull Collection<? extends PartyMember> partyMembers,
        @NotNull Wrapper<PartyMember> owner, @NotNull PartyNotificationConfig config,
        @NotNull TickFormatter tickFormatter, @NotNull MiniMessage miniMessage) {
        Objects.requireNonNull(partyMembers);
        this.audience = (ForwardingAudience) () -> {
            Collection<Audience> audiences = new ArrayList<>(partyMembers.size());

            for (PartyMember member : partyMembers) {
                member.getPlayerView().getPlayer().ifPresent(audiences::add);
            }

            return audiences;
        };
        this.owner = Objects.requireNonNull(owner);
        this.config = Objects.requireNonNull(config);
        this.tickFormatter = Objects.requireNonNull(tickFormatter);
        this.miniMessage = Objects.requireNonNull(miniMessage);
    }

    @Override
    public void notifyJoin(@NotNull PartyMember newMember) {
        newMember.getPlayerView().getDisplayName().thenAccept(displayName -> {
            TagResolver joinerPlaceholder = Placeholder.component("joiner", displayName);
            Component message = miniMessage.deserialize(config.joinToPartyFormat(), joinerPlaceholder);

            audience.sendMessage(message);
        });

        owner.get().getPlayerView().getDisplayName().thenAccept(displayName -> {
            newMember.getPlayerView().getPlayer().ifPresent(player -> {
                TagResolver ownerPlaceholder = Placeholder.component("owner", displayName);
                Component message = miniMessage.deserialize(config.joinToJoinerFormat(), ownerPlaceholder);
                player.sendMessage(message);
            });
        });
    }

    @Override
    public void notifyInvitation(@NotNull PartyMember inviter, @NotNull PlayerView invitee, long invitationDuration) {
        CompletableFuture<? extends Component> inviterDisplayName = inviter.getPlayerView().getDisplayName();
        CompletableFuture<? extends Component> inviteeDisplayName = invitee.getDisplayName();

        CompletableFuture.allOf(inviterDisplayName, inviteeDisplayName).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending invitation message", throwable);
                return;
            }

            TagResolver inviterPlaceholder = Placeholder.component("inviter", inviterDisplayName.join());
            TagResolver inviteePlaceholder = Placeholder.component("invitee", inviteeDisplayName.join());
            TagResolver durationPlaceholder =
                Placeholder.unparsed("duration", tickFormatter.format(invitationDuration));
            Component message =
                miniMessage.deserialize(config.inviteToPartyFormat(), inviterPlaceholder, inviteePlaceholder,
                    durationPlaceholder);
            audience.sendMessage(message);
        });

        if (inviter == owner.get()) {
            CompletableFuture<String> ownerUsername = inviter.getPlayerView().getUsername();
            CompletableFuture.allOf(ownerUsername, inviterDisplayName).whenComplete((ignored, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Exception while sending invitation message", throwable);
                    return;
                }

                invitee.getPlayer().ifPresent(player -> {
                    TagResolver ownerUsernamePlaceholder = Placeholder.parsed("owner-username", ownerUsername.join());
                    TagResolver ownerPlaceholder = Placeholder.component("owner", inviterDisplayName.join());
                    Component message =
                        miniMessage.deserialize(config.inviteToInviteeFromOwnerFormat(), ownerUsernamePlaceholder,
                            ownerPlaceholder);
                    player.sendMessage(message);
                });
            });
        } else {
            CompletableFuture<String> ownerUsername = owner.get().getPlayerView().getUsername();
            CompletableFuture<? extends Component> ownerDisplayName = owner.get().getPlayerView().getDisplayName();
            CompletableFuture.allOf(ownerUsername, ownerDisplayName, inviterDisplayName)
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Exception while sending invitation message", throwable);
                        return;
                    }

                    invitee.getPlayer().ifPresent(player -> {
                        TagResolver ownerUsernamePlaceholder =
                            Placeholder.parsed("owner-username", ownerUsername.join());
                        TagResolver ownerPlaceholder = Placeholder.component("owner", ownerDisplayName.join());
                        TagResolver inviterPlaceholder =
                            Placeholder.component("inviter", inviterDisplayName.join());
                        TagResolver durationPlaceholder = Formatter.date("duration",
                            Duration.of(invitationDuration, TimeUnit.SERVER_TICK).addTo(LocalDate.EPOCH));

                        Component message = miniMessage.deserialize(config.inviteToInviteeFromOtherFormat(),
                            ownerUsernamePlaceholder, ownerPlaceholder, inviterPlaceholder,
                            durationPlaceholder);
                        player.sendMessage(message);
                    });
                });
        }
    }

    @Override
    public void notifyExpiry(@NotNull PlayerView inviter, @NotNull PlayerView invitee) {
        invitee.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending invitation message", throwable);
                return;
            }

            TagResolver inviteePlaceholder = Placeholder.component("invitee", displayName);
            Component message = miniMessage.deserialize(config.expiryToPartyFormat(), inviteePlaceholder);
            audience.sendMessage(message);
        });

        inviter.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending invitation expiry message", throwable);
                return;
            }

            invitee.getPlayer().ifPresent(player -> {
                TagResolver inviterPlaceholder = Placeholder.component("inviter", displayName);
                Component message = miniMessage.deserialize(config.expiryToInviteeFormat(), inviterPlaceholder);
                player.sendMessage(message);
            });
        });
    }

    public void notifyLeave(@NotNull PartyMember oldMember) {
        oldMember.getPlayerView().getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending party leave message", throwable);
                return;
            }

            TagResolver leaverPlaceholder = Placeholder.component("leaver", displayName);
            Component message = miniMessage.deserialize(config.leaveToPartyFormat(), leaverPlaceholder);
            audience.sendMessage(message);
        });

        oldMember.getPlayerView().getPlayer().ifPresent(player -> {
            Component message = miniMessage.deserialize(config.leaveToLeaverFormat());
            player.sendMessage(message);
        });
    }

    public void notifyKick(@NotNull PartyMember kicker, @NotNull PartyMember toKick) {
        CompletableFuture<? extends Component> kickerName = kicker.getPlayerView().getDisplayName();
        CompletableFuture<? extends Component> toKickName = toKick.getPlayerView().getDisplayName();

        CompletableFuture.allOf(kickerName, toKickName).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending kick message", throwable);
                return;
            }

            TagResolver kickerPlaceholder = Placeholder.component("kicker", kickerName.join());
            TagResolver kickedPlaceholder = Placeholder.component("kicked", toKickName.join());
            Component message =
                miniMessage.deserialize(config.kickToPartyFormat(), kickerPlaceholder, kickedPlaceholder);
            audience.sendMessage(message);
        });

        kickerName.whenComplete((name, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending kick message", throwable);
                return;
            }

            toKick.getPlayerView().getPlayer().ifPresent(player -> {
                TagResolver kickerPlaceholder = Placeholder.component("kicker", name);
                Component message = miniMessage.deserialize(config.kickToKickedFormat(), kickerPlaceholder);
                player.sendMessage(message);
            });
        });
    }

    public void notifyTransfer(@NotNull PartyMember from, @NotNull PartyMember to) {
        CompletableFuture<? extends Component> fromDisplayName = from.getPlayerView().getDisplayName();
        CompletableFuture<? extends Component> toDisplayName = to.getPlayerView().getDisplayName();

        CompletableFuture.allOf(fromDisplayName, toDisplayName).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending transfer message", throwable);
                return;
            }

            TagResolver fromPlaceholder = Placeholder.component("from", fromDisplayName.join());
            TagResolver toPlaceholder = Placeholder.component("to", toDisplayName.join());
            Component message = miniMessage.deserialize(config.transferFormat(), fromPlaceholder, toPlaceholder);
            audience.sendMessage(message);
        });
    }

    public void notifyDisband() {
        owner.get().getPlayerView().getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while sending party disband message", throwable);
                return;
            }

            TagResolver ownerPlaceholder = Placeholder.component("owner", displayName);
            Component message = miniMessage.deserialize(config.disbandFormat(), ownerPlaceholder);
            audience.sendMessage(message);
        });
    }

}
