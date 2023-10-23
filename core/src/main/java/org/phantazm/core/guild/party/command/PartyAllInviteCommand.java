package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Flags;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;

import java.util.Objects;
import java.util.UUID;

public class PartyAllInviteCommand {
    
    private PartyAllInviteCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command allInviteCommand(@NotNull PartyCommandConfig config,
        @NotNull GuildHolder<Party> partyHolder) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(partyHolder);

        Command command = new Command("allinvite");
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

            PartyMember member = party.getMemberManager().getMember(player.getUuid());
            if (!party.getAllInvitePermission().hasPermission(member)) {
                sender.sendMessage(config.cannotInvitePlayers());
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID uuid = ((Player) sender).getUuid();
            Party party = partyHolder.uuidToGuild().get(uuid);
            PartyMember member = party.getMemberManager().getMember(uuid);

            boolean enabled = party.getFlaggable().toggleFlag(Flags.ALL_INVITE);
            party.getNotification().notifyAllInvite(member, enabled);
        });

        return command;
    }
    
}
