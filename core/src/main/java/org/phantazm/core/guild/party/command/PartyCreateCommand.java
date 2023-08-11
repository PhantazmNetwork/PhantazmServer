package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Objects;

public class PartyCreateCommand {

    private PartyCreateCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command createCommand(@NotNull PartyCommandConfig config,
            @NotNull GuildHolder<Party> partyHolder, @NotNull PlayerViewProvider viewProvider,
            @NotNull PartyCreator partyCreator) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(partyHolder, "partyHolder");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(partyCreator, "partyCreator");

        Command command = new Command("create");
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
                sender.sendMessage(config.alreadyInParty());
                return false;
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player)sender;
            Party party = partyCreator.createPartyFor(viewProvider.fromPlayer(player));
            partyHolder.guilds().add(party);
            partyHolder.uuidToGuild().put(player.getUuid(), party);

            sender.sendMessage(config.createCommandSuccess());
        });

        return command;
    }

}
