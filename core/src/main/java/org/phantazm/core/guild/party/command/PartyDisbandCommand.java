package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;

import java.util.Objects;
import java.util.UUID;

public class PartyDisbandCommand {

    private PartyDisbandCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command disbandCommand(@NotNull PartyCommandConfig config,
        @NotNull GuildHolder<Party> partyHolder) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(partyHolder);

        Command command = new Command("disband");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            Party party = partyHolder.uuidToGuild().get(player.getUuid());
            if (party == null) {
                sender.sendMessage(config.notInParty());
                return false;
            }

            if (!party.getOwner().get().getPlayerView().getUUID().equals(player.getUuid())) {
                sender.sendMessage(config.mustBeOwner());
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID uuid = ((Player) sender).getUuid();
            Party party = partyHolder.uuidToGuild().get(uuid);

            party.getNotification().notifyDisband();

            for (UUID memberUUID : party.getMemberManager().getMembers().keySet()) {
                partyHolder.uuidToGuild().remove(memberUUID);
            }
            partyHolder.guilds().remove(party);
        });

        return command;
    }

}
