package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.network.ConnectionManager;
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

    public static @NotNull Command partyCommand(@NotNull PartyCommandConfig config,
        @NotNull ConnectionManager connectionManager, @NotNull MiniMessage miniMessage,
        @NotNull GuildHolder<Party> partyHolder, @NotNull PlayerViewProvider viewProvider,
        @NotNull PartyCreator partyCreator, @NotNull Random random, int creatorRank, int defaultRank) {
        Command command = new Command("party", "p");
        command.addSubcommand(PartyCreateCommand.createCommand(config, partyHolder, viewProvider, partyCreator));
        command.addSubcommand(
            PartyJoinCommand.joinCommand(config, miniMessage, partyHolder.uuidToGuild(), viewProvider));
        command.addSubcommand(PartyLeaveCommand.leaveCommand(config, partyHolder.uuidToGuild(), random, creatorRank));
        command.addSubcommand(
            PartyKickCommand.kickCommand(config, miniMessage, partyHolder.uuidToGuild(), viewProvider));
        command.addSubcommand(
            PartyInviteCommand.inviteCommand(config, connectionManager, miniMessage, partyHolder, viewProvider,
                partyCreator));
        command.addSubcommand(PartyListCommand.listCommand(config, miniMessage, partyHolder.uuidToGuild()));
        command.addSubcommand(
            PartyTransferCommand.transferCommand(config, miniMessage, partyHolder.uuidToGuild(), viewProvider,
                creatorRank, defaultRank));
        command.addSubcommand(PartyDisbandCommand.disbandCommand(config, partyHolder));
        command.addSubcommand(
            PartySpyCommand.spyCommand(config, miniMessage, connectionManager, partyHolder, viewProvider));
        command.addSubcommand(PartyAllInviteCommand.allInviteCommand(config, partyHolder));

        return command;
    }

}
