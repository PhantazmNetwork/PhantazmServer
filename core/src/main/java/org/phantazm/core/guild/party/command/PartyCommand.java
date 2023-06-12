package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.UUID;

public class PartyCommand {

    public static Command command(@NotNull Map<? super UUID, Party> parties, @NotNull PlayerViewProvider viewProvider,
            @NotNull PartyCreator partyCreator) {
        Command command = new Command("party");
        command.addSubcommand(PartyCreateCommand.createCommand(parties, viewProvider, partyCreator));
        command.addSubcommand(PartyJoinCommand.joinCommand(parties, viewProvider));
        command.addSubcommand(PartyLeaveCommand.leaveCommand(parties));
        command.addSubcommand(PartyKickCommand.kickCommand(parties, viewProvider));

        return command;
    }

}
