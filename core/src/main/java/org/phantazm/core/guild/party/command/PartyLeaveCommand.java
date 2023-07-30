package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;

import java.util.*;

public class PartyLeaveCommand {

    private PartyLeaveCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command leaveCommand(@NotNull PartyCommandConfig config,
            @NotNull Map<? super UUID, ? extends Party> partyMap, @NotNull Random random, int creatorRank) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(partyMap, "partyMap");
        Objects.requireNonNull(random, "random");

        Command command = new Command("leave");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(config.notInParty());
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
                }
                else {
                    Collection<PartyMember> online = new ArrayList<>(), offline = new ArrayList<>();
                    for (PartyMember member : party.getMemberManager().getMembers().values()) {
                        if (member.getPlayerView().getPlayer().isPresent()) {
                            online.add(member);
                        }
                        else {
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
                    newOwner.setRank(creatorRank);
                    party.getNotification().notifyTransfer(oldMember, newOwner);
                }
            }
        });

        return command;
    }

}
