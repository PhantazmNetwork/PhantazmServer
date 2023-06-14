package org.phantazm.core.guild.party;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.invite.InvitationNotification;
import org.phantazm.core.player.PlayerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PartyNotification implements InvitationNotification<PartyMember> {

    private final Audience audience;

    private final Wrapper<PartyMember> owner;

    public PartyNotification(@NotNull Collection<? extends PartyMember> partyMembers,
            @NotNull Wrapper<PartyMember> owner) {
        Objects.requireNonNull(partyMembers, "partyMembers");
        this.audience = (ForwardingAudience)() -> {
            Collection<Audience> audiences = new ArrayList<>(partyMembers.size());

            for (PartyMember member : partyMembers) {
                member.getPlayerView().getPlayer().ifPresent(audiences::add);
            }

            return audiences;
        };
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public void notifyJoin(@NotNull PartyMember newMember) {
        newMember.getPlayerView().getDisplayName().thenAccept(displayName -> {
            ComponentLike message = Component.text().append(displayName, Component.text(" has joined the party."))
                    .color(NamedTextColor.GOLD);
            audience.sendMessage(message);
        });

        owner.get().getPlayerView().getDisplayName().thenAccept(displayName -> {
            newMember.getPlayerView().getPlayer().ifPresent(player -> {
                ComponentLike message =
                        Component.text().append(Component.text("Joined "), displayName, Component.text("'s party."))
                                .color(NamedTextColor.GREEN);
                player.sendMessage(message);
            });
        });
    }

    @Override
    public void notifyInvitation(@NotNull PartyMember inviter, @NotNull PlayerView invitee) {
        CompletableFuture<? extends Component> inviterName = inviter.getPlayerView().getDisplayName();
        CompletableFuture<? extends Component> inviteeName = invitee.getDisplayName();

        CompletableFuture.allOf(inviterName, inviteeName).thenRun(() -> {
            ComponentLike message = Component.text()
                    .append(inviterName.join(), Component.text(" has invited "), inviteeName.join(),
                            Component.text(" to the party.")).color(NamedTextColor.GOLD);
            audience.sendMessage(message);
        });

        if (inviter == owner.get()) {
            inviterName.thenAccept(displayName -> {
                invitee.getPlayer().ifPresent(player -> {
                    ComponentLike message = Component.text()
                            .append(displayName, Component.text(" has invited you to join their party. Click "),
                                    Component.text("here", NamedTextColor.GOLD)
                                            .clickEvent(ClickEvent.runCommand("/party join " + player.getUsername())),
                                    Component.text(" to join!")).color(NamedTextColor.GOLD);
                    player.sendMessage(message);
                });
            });
        }
        else {
            CompletableFuture<String> ownerName = owner.get().getPlayerView().getUsername();
            CompletableFuture<? extends Component> ownerDisplayName = owner.get().getPlayerView().getDisplayName();
            CompletableFuture.allOf(ownerName, ownerDisplayName, inviterName).thenRun(() -> {
                invitee.getPlayer().ifPresent(player -> {
                    ComponentLike message = Component.text()
                            .append(inviterName.join(), Component.text(" has invited you to join "),
                                    ownerDisplayName.join(), Component.text("'s party. Click "),
                                    Component.text("here", NamedTextColor.GOLD)
                                            .clickEvent(ClickEvent.runCommand("/party join " + ownerName.join())),
                                    Component.text(" to join!")).color(NamedTextColor.GOLD);
                    player.sendMessage(message);
                });
            });
        }
    }

    @Override
    public void notifyExpiry(@NotNull PlayerView invitee) {
        invitee.getDisplayName().thenAccept(displayName -> {
            ComponentLike message = Component.text()
                    .append(Component.text("The invitation to "), displayName, Component.text(" has expired."))
                    .color(NamedTextColor.GOLD);
            audience.sendMessage(message);
        });

        owner.get().getPlayerView().getDisplayName().thenAccept(displayName -> {
            invitee.getPlayer().ifPresent(player -> {
                ComponentLike message = Component.text().append(Component.text("The invitation to "), displayName,
                        Component.text("'s party has expired.")).color(NamedTextColor.GOLD);
                player.sendMessage(message);
            });
        });
    }

    public void notifyLeave(@NotNull PartyMember oldMember) {
        oldMember.getPlayerView().getDisplayName().thenAccept(displayName -> {
            ComponentLike message = Component.text().append(displayName, Component.text(" has left the party."))
                    .color(NamedTextColor.GOLD);
            audience.sendMessage(message);
        });

        oldMember.getPlayerView().getPlayer().ifPresent(player -> {
            player.sendMessage(Component.text("Left the party.", NamedTextColor.GREEN));
        });
    }

    public void notifyKick(@NotNull PartyMember kicker, @NotNull PartyMember toKick) {
        CompletableFuture<? extends Component> kickerName = kicker.getPlayerView().getDisplayName();
        CompletableFuture<? extends Component> toKickName = toKick.getPlayerView().getDisplayName();

        CompletableFuture.allOf(kickerName, toKickName).thenRun(() -> {
            ComponentLike message = Component.text()
                    .append(toKickName.join(), Component.text(" was kicked by "), kickerName.join(),
                            Component.text(".")).color(NamedTextColor.GOLD);
            audience.sendMessage(message);
        });

        kickerName.thenAccept(name -> {
            toKick.getPlayerView().getPlayer().ifPresent(player -> {
                ComponentLike message =
                        Component.text().append(Component.text("You were kicked by "), name, Component.text("."))
                                .color(NamedTextColor.GOLD);
                player.sendMessage(message);
            });
        });
    }

}
