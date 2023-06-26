package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
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

    public static @NotNull Command kickCommand(@NotNull PartyCommandConfig config, @NotNull MiniMessage miniMessage,
            @NotNull Map<? super UUID, ? extends Party> partyMap, @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(miniMessage, "miniMessage");
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
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(config.notInParty());
                return false;
            }

            PartyMember member = party.getMemberManager().getMember(player.getUuid());
            if (!party.getKickPermission().hasPermission(member)) {
                sender.sendMessage(config.cannotKickMembers());
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
                            TagResolver toKickPlaceholder = Placeholder.component("kicked", displayName);
                            Component message =
                                    miniMessage.deserialize(config.toKickNotInPartyFormat(), toKickPlaceholder);
                            sender.sendMessage(message);
                        });
                        return;
                    }

                    if (toKick == kicker) {
                        sender.sendMessage(config.cannotKickSelf());
                    }

                    if (!party.getKickPermission().canExecute(kicker, toKick)) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            TagResolver kickedPlaceholder = Placeholder.component("kicked", displayName);
                            Component message =
                                    miniMessage.deserialize(config.cannotKickOtherFormat(), kickedPlaceholder);
                            sender.sendMessage(message);
                        });
                        return;
                    }

                    party.getMemberManager().removeMember(playerView.getUUID());
                    partyMap.remove(playerView.getUUID());
                    party.getNotification().notifyKick(kicker, toKick);
                }, () -> {
                    TagResolver usernamePlaceholder = Placeholder.unparsed("username", name);
                    sender.sendMessage(miniMessage.deserialize(config.cannotFindPlayerFormat(), usernamePlaceholder));
                });
            });
        }, nameArgument);

        return command;
    }

}
