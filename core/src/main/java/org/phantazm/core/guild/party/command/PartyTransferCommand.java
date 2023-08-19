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
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyTransferCommand {

    private PartyTransferCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command transferCommand(@NotNull PartyCommandConfig config, @NotNull MiniMessage miniMessage,
        @NotNull Map<? super UUID, ? extends Party> partyMap, @NotNull PlayerViewProvider viewProvider,
        int creatorRank, int defaultRank) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(miniMessage);
        Objects.requireNonNull(partyMap);
        Objects.requireNonNull(viewProvider);

        Command command = new Command("transfer");
        Argument<String> nameArgument = ArgumentType.Word("name");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null || !party.getOwner().get().getPlayerView().getUUID().equals(player.getUuid())) {
                return;
            }

            String prefix = context.getOrDefault(nameArgument, "").trim().toLowerCase();
            PartyMember member = party.getMemberManager().getMember(player.getUuid());
            for (PartyMember otherMember : party.getMemberManager().getMembers().values()) {
                if (otherMember != member) {
                    otherMember.getPlayerView().getUsernameIfCached().ifPresent(username -> {
                        if (username.toLowerCase().startsWith(prefix)) {
                            suggestion.addEntry(new SuggestionEntry(username));
                        }
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

            if (!party.getOwner().get().getPlayerView().getUUID().equals(player.getUuid())) {
                sender.sendMessage(config.mustBeOwner());
                return false;
            }

            return true;
        }, (sender, context) -> {
            String name = context.get(nameArgument);

            UUID uuid = ((Player) sender).getUuid();
            Party party = partyMap.get(uuid);
            PartyMember oldOwner = party.getMemberManager().getMember(uuid);

            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    PartyMember newOwner = party.getMemberManager().getMember(playerView.getUUID());
                    if (newOwner == null) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            TagResolver toTransferPlaceholder = Placeholder.component("new_owner", displayName);
                            Component message =
                                miniMessage.deserialize(config.toTransferNotInPartyFormat(), toTransferPlaceholder);
                            sender.sendMessage(message);
                        });
                        return;
                    }

                    if (newOwner == oldOwner) {
                        sender.sendMessage(config.cannotTransferToSelf());
                        return;
                    }

                    party.getOwner().set(newOwner);
                    newOwner.setRank(creatorRank);
                    oldOwner.setRank(defaultRank);
                    party.getNotification().notifyTransfer(oldOwner, newOwner);
                }, () -> {
                    TagResolver usernamePlaceholder = Placeholder.unparsed("username", name);
                    sender.sendMessage(miniMessage.deserialize(config.cannotFindPlayerFormat(), usernamePlaceholder));
                });
            });
        }, nameArgument);

        return command;
    }

}
