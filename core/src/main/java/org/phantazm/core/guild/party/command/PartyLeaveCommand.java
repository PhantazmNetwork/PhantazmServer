package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;

import java.util.*;

public class PartyLeaveCommand {

    private PartyLeaveCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command leaveCommand(@NotNull Map<? super UUID, ? extends Party> partyMap,
            @NotNull Random random) {
        Objects.requireNonNull(partyMap, "partyMap");
        Objects.requireNonNull(random, "random");

        Command command = new Command("leave");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(Component.text("You have to be in a party!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID uuid = ((Player)sender).getUuid();
            Party party = partyMap.remove(uuid);
            PartyMember oldMember = party.getMemberManager().removeMember(uuid);

            party.getNotification().notifyLeave(oldMember);

            if (party.getOwner().get().getPlayerView().getUUID().equals(uuid)) {
                if (party.getMemberManager().getMembers().isEmpty()) {
                    party.getOwner().set(null);
                } else {
                    Collection<PartyMember> online = new ArrayList<>(), offline = new ArrayList<>();
                    for (PartyMember member : party.getMemberManager().getMembers().values()) {
                        if (member.getPlayerView().getPlayer().isPresent()) {
                            online.add(member);
                        } else {
                            offline.add(member);
                        }
                    }

                    Collection<PartyMember> candidates = online.size() != 0 ? online : offline;
                    int memberIndex = random.nextInt(candidates.size());
                    Iterator<PartyMember> memberIterator = candidates.iterator();
                    PartyMember newOwner = memberIterator.next();
                    for (int i = 0; i < memberIndex; ++i) {
                        newOwner = memberIterator.next();
                    }

                    party.getOwner().set(newOwner);
                    party.getNotification().notifyTransfer(oldMember, newOwner);
                }
            }
        });

        return command;
    }

}
