package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyLeaveCommand {

    public static Command leaveCommand(@NotNull Map<? super UUID, ? extends Party> parties) {
        Objects.requireNonNull(parties, "parties");

        Command command = new Command("leave");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = parties.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(Component.text("You have to be in a party!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID uuid = ((Player)sender).getUuid();
            Party party = parties.remove(uuid);
            PartyMember oldMember = party.getMemberManager().removeMember(uuid);

            party.getNotification().notifyLeave(oldMember);
            sender.sendMessage(Component.text("Left the party.", NamedTextColor.GREEN));
        });

        return command;
    }

}
