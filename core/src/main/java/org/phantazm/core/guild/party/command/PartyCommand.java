package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PartyCommand {

    private PartyCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command partyCommand(@NotNull Map<? super UUID, Party> parties, @NotNull PlayerViewProvider viewProvider,
            @NotNull PartyCreator partyCreator, @NotNull Random random) {
        Command command = new Command("party", "p");
        command.addSubcommand(PartyCreateCommand.createCommand(parties, viewProvider, partyCreator));
        command.addSubcommand(PartyJoinCommand.joinCommand(parties, viewProvider));
        command.addSubcommand(PartyLeaveCommand.leaveCommand(parties, random));
        command.addSubcommand(PartyKickCommand.kickCommand(parties, viewProvider));
        command.addSubcommand(PartyInviteCommand.inviteCommand(parties, viewProvider));
        command.addSubcommand(PartyListCommand.listCommand(parties));

        return command;
    }

}
