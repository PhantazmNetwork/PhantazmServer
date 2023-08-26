package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyJoinCommand {

    private PartyJoinCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command joinCommand(@NotNull PartyCommandConfig config, @NotNull MiniMessage miniMessage,
        @NotNull Map<? super UUID, Party> partyMap, @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(miniMessage);
        Objects.requireNonNull(partyMap);
        Objects.requireNonNull(viewProvider);

        Argument<String> nameArgument = ArgumentType.Word("name");
        Command command = new Command("join");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party != null) {
                sender.sendMessage(config.alreadyInParty());
                return false;
            }

            return true;
        }, (sender, context) -> {
            String name = context.get(nameArgument);
            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                playerViewOptional.ifPresentOrElse(playerView -> {
                    Party party = partyMap.get(playerView.getUUID());
                    if (party == null) {
                        playerView.getDisplayName().thenAccept(displayName -> {
                            TagResolver targetPlaceholder = Placeholder.component("target", displayName);
                            Component message = miniMessage.deserialize(config.toJoinNotInParty(), targetPlaceholder);
                            sender.sendMessage(message);
                        });
                        return;
                    }

                    Player player = (Player) sender;
                    if (!party.getInvitationManager().hasInvitation(player.getUuid())) {
                        sender.sendMessage(config.noInvite());
                        return;
                    }

                    Party previousParty = partyMap.get(player.getUuid());
                    if (previousParty != null) {
                        previousParty.getMemberManager().removeMember(player.getUuid());
                    }

                    party.getInvitationManager().acceptInvitation(viewProvider.fromPlayer(player));
                    partyMap.put(player.getUuid(), party);
                }, () -> {
                    TagResolver usernamePlaceholder = Placeholder.unparsed("username", name);
                    sender.sendMessage(miniMessage.deserialize(config.cannotFindPlayerFormat(), usernamePlaceholder));
                });
            });
        }, nameArgument);

        return command;
    }

}
