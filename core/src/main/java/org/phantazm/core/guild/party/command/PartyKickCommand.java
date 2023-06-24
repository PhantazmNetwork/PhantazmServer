package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyKickCommand {

    private PartyKickCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command kickCommand(@NotNull Map<? super UUID, ? extends Party> partyMap,
            @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(partyMap, "partyMap");
        Objects.requireNonNull(viewProvider, "viewProvider");

        Command command = new Command("kick");
        Argument<String> nameArgument = ArgumentType.Word("name");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null) {
                return;
            }

            PartyMember member = party.getMemberManager().getMember(player.getUuid());
            MultipleMemberPermission<PartyMember> permission = party.getKickPermission();
            if (!permission.hasPermission(member)) {
                return;
            }

            for (PartyMember otherMember : party.getMemberManager().getMembers().values()) {
                if (otherMember != member && permission.canExecute(member, otherMember)) {
                    otherMember.getPlayerView().getUsernameIfCached().ifPresent(username -> {
                        suggestion.addEntry(new SuggestionEntry(username));
                    });
                }
            }
        });

        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(Component.text("You have to be in a party!", NamedTextColor.RED));
                return false;
            }

            PartyMember member = party.getMemberManager().getMember(player.getUuid());
            if (!party.getKickPermission().hasPermission(member)) {
                sender.sendMessage(Component.text("You can't kick members!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            String name = context.get(nameArgument);

            UUID uuid = ((Player)sender).getUuid();
            Party party = partyMap.get(uuid);
            PartyMember kicker = party.getMemberManager().getMember(uuid);

            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    PartyMember toKick = party.getMemberManager().getMember(playerView.getUUID());
                    if (toKick == null) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            sender.sendMessage(
                                    Component.text().append(displayName, Component.text(" is not in the party."))
                                            .color(NamedTextColor.RED));
                        });
                        return;
                    }

                    if (toKick == kicker) {
                        sender.sendMessage(Component.text("You can't kick yourself!", NamedTextColor.RED));
                    }

                    if (!party.getKickPermission().canExecute(kicker, toKick)) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            sender.sendMessage(Component.text()
                                    .append(Component.text("You can't kick "), displayName, Component.text("!"))
                                    .color(NamedTextColor.RED));
                        });
                        return;
                    }

                    party.getMemberManager().removeMember(playerView.getUUID());
                    partyMap.remove(playerView.getUUID());
                    party.getNotification().notifyKick(kicker, toKick);
                }, () -> {
                    sender.sendMessage(
                            Component.text("Can't find anyone with the username " + name + "!", NamedTextColor.RED));
                });
            });
        }, nameArgument);

        return command;
    }

}
