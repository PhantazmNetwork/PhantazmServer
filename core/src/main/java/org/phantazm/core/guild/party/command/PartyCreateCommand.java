package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyCreateCommand {

    private PartyCreateCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command createCommand(@NotNull Map<? super UUID, Party> parties,
            @NotNull PlayerViewProvider viewProvider, @NotNull PartyCreator partyCreator) {
        Objects.requireNonNull(parties, "parties");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(partyCreator, "partyCreator");

        Command command = new Command("create");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

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

}
