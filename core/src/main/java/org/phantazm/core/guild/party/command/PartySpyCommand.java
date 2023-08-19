package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.SpyAudience;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Objects;

public class PartySpyCommand {

    public static final Permission PERMISSION = new Permission("admin.spy");

    private PartySpyCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command spyCommand(@NotNull PartyCommandConfig config, @NotNull MiniMessage miniMessage,
            @NotNull ConnectionManager connectionManager, @NotNull GuildHolder<Party> partyHolder,
            @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(miniMessage);
        Objects.requireNonNull(connectionManager);
        Objects.requireNonNull(partyHolder);
        Objects.requireNonNull(viewProvider);

        Command command = new Command("spy");
        Argument<String> nameArgument = ArgumentType.String("name");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            String prefix = context.get(nameArgument).trim().toLowerCase();

            for (Player player : connectionManager.getOnlinePlayers()) {
                String username = player.getUsername();
                if (username.toLowerCase().startsWith(prefix)) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            }
        });

        command.setCondition((sender, commandString) -> {
            if (!sender.hasPermission(PERMISSION)) {
                return false;
            }

            return sender instanceof Player || sender instanceof ConsoleSender;
        });
        command.addSyntax((sender, context) -> {
            String name = context.get(nameArgument);

            viewProvider.fromName(name).thenAccept(playerViewOptional -> {
                if (playerViewOptional.isEmpty()) {
                    sender.sendMessage(config.mustBeAPlayer());
                    return;
                }

                PlayerView playerView = playerViewOptional.get();
                Party party = partyHolder.uuidToGuild().get(playerView.getUUID());
                if (party == null) {
                    playerView.getDisplayName().thenAccept(displayName -> {
                        TagResolver targetPlaceholder = Placeholder.component("target", displayName);
                        sender.sendMessage(miniMessage.deserialize(config.toSpyNotInPartyFormat(), targetPlaceholder));
                    });
                    return;
                }

                SpyAudience spyAudience = party.getSpyAudience();
                boolean nowSpying;
                if (sender instanceof Player player) {
                    PlayerView senderView = viewProvider.fromPlayer(player);
                    nowSpying = !spyAudience.hasPlayerSpy(senderView.getUUID());
                    if (nowSpying) {
                        spyAudience.addPlayerSpy(senderView);
                    }
                    else {
                        spyAudience.removePlayerSpy(senderView.getUUID());
                    }
                }
                else {
                    nowSpying = !spyAudience.hasExtraSpy(sender);
                    if (nowSpying) {
                        spyAudience.addExtraSpy(sender);
                    }
                    else {
                        spyAudience.removeExtraSpy(sender);
                    }
                }

                playerView.getDisplayName().thenAccept(displayName -> {
                    TagResolver targetPlaceholder = Placeholder.component("target", displayName);

                    Component message;
                    if (nowSpying) {
                        message = miniMessage.deserialize(config.nowSpyingFormat(), targetPlaceholder);
                    }
                    else {
                        message = miniMessage.deserialize(config.noLongerSpyingFormat(), targetPlaceholder);
                    }

                    sender.sendMessage(message);
                });
            });
        }, nameArgument);

        return command;
    }

}
