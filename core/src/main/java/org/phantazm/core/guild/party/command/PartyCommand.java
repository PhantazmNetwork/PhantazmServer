package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyCommand {

    public static Command command(@NotNull Map<? super UUID, Party> parties, @NotNull PlayerViewProvider viewProvider,
            @NotNull PartyCreator partyCreator) {
        Command command = new Command("party");
        command.addSubcommand(createCommand(parties, viewProvider, partyCreator));
        command.addSubcommand(joinCommand(parties, viewProvider));
        command.addSubcommand(leaveCommand(parties));
        command.addSubcommand(kickCommand(parties, viewProvider));

        return command;
    }

    private static Command createCommand(@NotNull Map<? super UUID, Party> parties,
            @NotNull PlayerViewProvider viewProvider, @NotNull PartyCreator partyCreator) {
        Objects.requireNonNull(parties, "parties");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(partyCreator, "partyCreator");

        Command command = new Command("create");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party != null) {
                sender.sendMessage(Component.text("You are already in a party!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player)sender;
            parties.put(player.getUuid(), partyCreator.createPartyFor(viewProvider.fromPlayer(player)));

            sender.sendMessage(Component.text("You are now in a party.", NamedTextColor.GREEN));
        });

        return command;
    }

    private static Command joinCommand(@NotNull Map<? super UUID, Party> parties,
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

    private static Command leaveCommand(@NotNull Map<? super UUID, ? extends Party> parties) {
        Objects.requireNonNull(parties, "parties");

        Command command = new Command("leave");
        command.addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(Component.text("You have to be in a party!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID uuid = ((Player)sender).getUuid();
            Party party = parties.remove(uuid);
            PartyMember oldMember = party.getMemberManager().removeMember(uuid);

            party.getNotification().notifyLeave(oldMember);
            sender.sendMessage(Component.text("Left the party.", NamedTextColor.GREEN));
        });

        return command;
    }

    private static Command kickCommand(@NotNull Map<? super UUID, ? extends Party> parties,
            @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(parties, "parties");
        Objects.requireNonNull(viewProvider, "viewProvider");

        Command command = new Command("kick");
        Argument<String> nameArgument = ArgumentType.Word("name");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            Party party = parties.get(player.getUuid());
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
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = parties.get(player.getUuid());
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
            Party party = parties.get(uuid);
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
                    parties.remove(playerView.getUUID());
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
