package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Random;

public class PartyCommand {

    private PartyCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command partyCommand(@NotNull GuildHolder<Party> partyHolder,
            @NotNull PlayerViewProvider viewProvider, @NotNull PartyCreator partyCreator, @NotNull Random random) {
        Command command = new Command("party", "p");
        command.addSubcommand(PartyCreateCommand.createCommand(partyHolder, viewProvider, partyCreator));
        command.addSubcommand(PartyJoinCommand.joinCommand(partyHolder.uuidToGuild(), viewProvider));
        command.addSubcommand(PartyLeaveCommand.leaveCommand(partyHolder.uuidToGuild(), random));
        command.addSubcommand(PartyKickCommand.kickCommand(partyHolder.uuidToGuild(), viewProvider));
        command.addSubcommand(PartyInviteCommand.inviteCommand(partyHolder, viewProvider, partyCreator));
        command.addSubcommand(PartyListCommand.listCommand(partyHolder.uuidToGuild()));

        return command;
    }

}
