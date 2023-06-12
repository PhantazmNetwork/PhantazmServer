package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyCommand {

    public static Command command(@NotNull Map<? super UUID, Party> parties, @NotNull PlayerViewProvider viewProvider) {
        Command command = new Command("party");
        command.addSubcommand(createCommand(parties, viewProvider));
        command.addSubcommand(joinCommand(parties, viewProvider));
        command.addSubcommand(leaveCommand(parties));

        return command;
    }

    private static Command createCommand(@NotNull Map<? super UUID, Party> parties,
            @NotNull PlayerViewProvider viewProvider) {
        Command command = new Command("create");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!"));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party != null) {
                sender.sendMessage(Component.text("You are already in a party"));
                return false;
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player)sender;
            Map<? super UUID, GuildMember> members = new HashMap<>();
            Party party = new Party(members);
            party.addMember(new GuildMember(viewProvider.fromPlayer(player)));
            parties.put(player.getUuid(), party);

            sender.sendMessage(Component.text("Ok"));
        });

        return command;
    }

    private static Command joinCommand(@NotNull Map<? super UUID, Party> parties,
            @NotNull PlayerViewProvider viewProvider) {
        Command command = new Command("join");

        Argument<String> nameArgument = new ArgumentWord("name");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!"));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party != null) {
                sender.sendMessage(Component.text("You are already in a party"));
                return false;
            }

            return true;
        }, (sender, context) -> {
            viewProvider.fromName(context.get(nameArgument)).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    Party party = parties.get(playerView.getUUID());
                    if (party == null) {
                        sender.sendMessage(Component.text("other player not in party"));
                        return;
                    }

                    Player player = (Player) sender;
                    Party previousParty = parties.get(player.getUuid());
                    if (previousParty != null) {
                        previousParty.removeMember(player.getUuid());
                    }

                    party.addMember(new GuildMember(viewProvider.fromPlayer(player)));
                    parties.put(player.getUuid(), party);

                    sender.sendMessage(Component.text("Ok"));
                }, () -> sender.sendMessage(Component.text("can't find")));
            });
        }, nameArgument);

        return command;
    }

    private static Command leaveCommand(@NotNull Map<? super UUID, ? extends Party> parties) {
        Command command = new Command("leave");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!"));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(Component.text("You have to be in a party"));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID uuid = ((Player)sender).getUuid();
            Party party = parties.remove(uuid);
            party.removeMember(uuid);

            sender.sendMessage(Component.text("Ok"));
        });

        return command;
    }

}
