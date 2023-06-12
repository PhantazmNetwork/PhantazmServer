package org.phantazm.core.guild.party;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PartyNotification {

    private final Audience audience;

    public PartyNotification(@NotNull Collection<? extends PartyMember> partyMembers) {
        Objects.requireNonNull(partyMembers, "partyMembers");
        this.audience = (ForwardingAudience)() -> {
            Collection<Audience> audiences = new ArrayList<>(partyMembers.size());

            for (PartyMember member : partyMembers) {
                member.getPlayerView().getPlayer().ifPresent(audiences::add);
            }

            return audiences;
        };
    }

    public void notifyJoin(@NotNull CommandSender sender, @NotNull PartyMember newMember, @NotNull PlayerView target) {
        newMember.getPlayerView().getDisplayName().thenAccept(displayName -> {
            ComponentLike message = Component.text().append(displayName, Component.text(" has joined the party."))
                    .color(NamedTextColor.GOLD);
            audience.sendMessage(message);
        });

        target.getDisplayName().thenAccept(displayName -> {
            sender.sendMessage(
                    Component.text().append(Component.text("Joined "), displayName, Component.text("'s party."))
                            .color(NamedTextColor.GREEN));
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
