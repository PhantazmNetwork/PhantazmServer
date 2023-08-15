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
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Objects;

public class PartyInviteCommand {

    private PartyInviteCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command inviteCommand(@NotNull PartyCommandConfig config,
            @NotNull ConnectionManager connectionManager, @NotNull MiniMessage miniMessage,
            @NotNull GuildHolder<Party> partyHolder, @NotNull PlayerViewProvider viewProvider,
            @NotNull PartyCreator partyCreator) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(miniMessage, "miniMessage");
        Objects.requireNonNull(partyHolder, "partyHolder");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(partyCreator, "partyCreator");

        Argument<String> nameArgument = ArgumentType.Word("name");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            String prefix = context.getOrDefault(nameArgument, "").trim().toLowerCase();

            if (sender instanceof Player player) {
                Party party = partyHolder.uuidToGuild().get(player.getUuid());
                if (party != null) {
                    for (Player otherPlayer : connectionManager.getOnlinePlayers()) {
                        if (party.getMemberManager().hasMember(otherPlayer.getUuid())) {
                            continue;
                        }

                        String username = otherPlayer.getUsername();
                        if (username.toLowerCase().startsWith(prefix)) {
                            suggestion.addEntry(new SuggestionEntry(username));
                        }
                    }

                    return;
                }
            }

            for (Player otherPlayer : connectionManager.getOnlinePlayers()) {
                if (otherPlayer == sender) {
                    continue;
                }

                String username = otherPlayer.getUsername();
                if (username.startsWith(prefix)) {
                    suggestion.addEntry(new SuggestionEntry(otherPlayer.getUsername()));
                }
            }
        });

        Command command = new Command("invite");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            Party party = partyHolder.uuidToGuild().get(player.getUuid());
            if (party != null) {
                PartyMember member = party.getMemberManager().getMember(player.getUuid());
                if (!party.getInvitePermission().hasPermission(member)) {
                    sender.sendMessage(config.cannotInvitePlayers());
                    return false;
                }
            }

            return true;
        }, (sender, context) -> {
            String name = context.get(nameArgument);

            Player player = ((Player)sender);
            Party tempParty = partyHolder.uuidToGuild().get(player.getUuid());
            if (tempParty == null) {
                tempParty = partyCreator.createPartyFor(viewProvider.fromPlayer(player));
                partyHolder.guilds().add(tempParty);
                partyHolder.uuidToGuild().put(player.getUuid(), tempParty);

                sender.sendMessage(config.automaticPartyCreation());
            }
            Party inviterParty = tempParty;

            PartyMember inviter = tempParty.getMemberManager().getMember(player.getUuid());

            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    if (playerView.getUUID().equals(player.getUuid())) {
                        sender.sendMessage(config.cannotInviteSelf());
                        return;
                    }

                    Party otherParty = partyHolder.uuidToGuild().get(playerView.getUUID());
                    if (otherParty == inviterParty) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            TagResolver inviteePlaceholder = Placeholder.component("invitee", displayName);
                            Component message =
                                    miniMessage.deserialize(config.inviteeAlreadyInPartyFormat(), inviteePlaceholder);
                            sender.sendMessage(message);
                        });
                        return;
                    }

                    if (playerView.getPlayer().isEmpty()) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            TagResolver playerPlaceholder = Placeholder.component("player", displayName);
                            sender.sendMessage(
                                    miniMessage.deserialize(config.playerNotOnlineFormat(), playerPlaceholder));
                        });
                        return;
                    }

                    if (inviterParty.getInvitationManager().hasInvitation(playerView.getUUID())) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            TagResolver inviteePlaceholder = Placeholder.component("invitee", displayName);
                            Component message = miniMessage.deserialize(config.inviteeAlreadyInvitedFormat(),
                                    inviteePlaceholder);
                            sender.sendMessage(message);
                        });
                        return;
                    }

                    inviterParty.getInvitationManager().invite(inviter, playerView);
                }, () -> {
                    TagResolver usernamePlaceholder = Placeholder.unparsed("username", name);
                    sender.sendMessage(miniMessage.deserialize(config.cannotFindPlayerFormat(), usernamePlaceholder));
                });
            });
        }, nameArgument);

        return command;
    }

}
