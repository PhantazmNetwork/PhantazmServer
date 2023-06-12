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

public class PartyJoinCommand {

    public static Command joinCommand(@NotNull Map<? super UUID, Party> parties,
            @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(parties, "parties");
        Objects.requireNonNull(viewProvider, "viewProvider");

        Command command = new Command("join");

        Argument<String> nameArgument = ArgumentType.Word("name");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party != null) {
                sender.sendMessage(Component.text("You are already in a party!", NamedTextColor.GREEN));
                return false;
            }

            return true;
        }, (sender, context) -> {
            String name = context.get(nameArgument);
            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    Party party = parties.get(playerView.getUUID());
                    if (party == null) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            sender.sendMessage(Component.text().append(displayName,
                                    Component.text(" is not in a party.").color(NamedTextColor.RED)));
                        });
                        return;
                    }

                    Player player = (Player)sender;
                    Party previousParty = parties.get(player.getUuid());
                    if (previousParty != null) {
                        previousParty.getMemberManager().removeMember(player.getUuid());
                    }

                    PartyMember newMember = party.getMemberCreator().apply(viewProvider.fromPlayer(player));
                    party.getMemberManager().addMember(newMember);
                    parties.put(player.getUuid(), party);

                    party.getNotification().notifyJoin(newMember);
                }, () -> {
                    sender.sendMessage(
                            Component.text("Can't find anyone with the username " + name + "!", NamedTextColor.RED));
                });
            });
        }, nameArgument);

        return command;
    }

}
