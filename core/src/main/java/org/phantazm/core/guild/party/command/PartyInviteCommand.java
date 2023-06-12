package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyInviteCommand {

    public static Command inviteCommand(@NotNull Map<? super UUID, ? extends Party> parties,
            @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(parties, "parties");
        Objects.requireNonNull(viewProvider, "viewProvider");

        Argument<String> nameArgument = ArgumentType.Word("name");

        Command command = new Command("invite");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(Component.text("You have to be in a party!", NamedTextColor.GREEN));
                return false;
            }

            PartyMember member = party.getMemberManager().getMember(player.getUuid());
            if (!party.getInvitePermission().hasPermission(member)) {
                sender.sendMessage(Component.text("You can't invite players!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            String name = context.get(nameArgument);

            UUID uuid = ((Player) sender).getUuid();
            Party inviterParty = parties.get(uuid);
            PartyMember inviter = inviterParty.getMemberManager().getMember(uuid);

            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    if (playerView.getUUID().equals(uuid)) {
                        sender.sendMessage(Component.text("You can't invite yourself!", NamedTextColor.RED));
                        return;
                    }

                    Party otherParty = parties.get(playerView.getUUID());
                    if (otherParty == inviterParty) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            sender.sendMessage(Component.text().append(displayName,
                                    Component.text(" is already in the party.").color(NamedTextColor.RED)));
                        });
                        return;
                    }

                    if (playerView.getPlayer().isEmpty()) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            sender.sendMessage(Component.text().append(displayName,
                                    Component.text(" is not online.")).color(NamedTextColor.RED));
                        });
                        return;
                    }

                    inviterParty.getInvitationManager().invite(inviter, playerView);
                }, () -> {
                    sender.sendMessage(
                            Component.text("Can't find anyone with the username " + name + "!", NamedTextColor.RED));
                });
            });
        }, nameArgument);

        return command;
    }

}
