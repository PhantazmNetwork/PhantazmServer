package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.core.guild.party.PartyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PartyListCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyListCommand.class);

    private static final CompletableFuture<?>[] EMPTY_COMPLETABLE_FUTURE_ARRAY = new CompletableFuture[0];

    private PartyListCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command listCommand(@NotNull PartyCommandConfig config,
            @NotNull Map<? super UUID, Party> partyMap) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(partyMap, "partyMap");

        Command command = new Command("list");
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

            return true;
        }, (sender, context) -> {
            Party party = partyMap.get(((Player)sender).getUuid());
            List<CompletableFuture<? extends Component>> displayNameFutures =
                    new ArrayList<>(party.getMemberManager().getMembers().size());
            for (PartyMember member : party.getMemberManager().getMembers().values()) {
                displayNameFutures.add(member.getPlayerView().getDisplayName());
            }

            CompletableFuture.allOf(displayNameFutures.toArray(EMPTY_COMPLETABLE_FUTURE_ARRAY))
                    .whenComplete((ignored, throwable) -> {
                        if (throwable != null) {
                            LOGGER.warn("Exception while sending list message", throwable);
                            return;
                        }

                        List<Component> displayNames = new ArrayList<>(displayNameFutures.size());
                        for (CompletableFuture<? extends Component> future : displayNameFutures) {
                            displayNames.add(future.join());
                        }

                        sender.sendMessage(
                                Component.join(JoinConfiguration.separator(Component.text(", ")), displayNames));
                    });
        });

        return command;
    }

}
