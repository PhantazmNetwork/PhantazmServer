package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyWarpCommand {

    public static @NotNull Command warpCommand(@NotNull Map<? super UUID, ? extends Party> partyMap,
        @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(partyMap);
        Objects.requireNonNull(viewProvider);

        Command command = new Command("warp");
        //        command.addConditionalSyntax((sender, commandString) -> {
        //            if (commandString == null) {
        //                return sender instanceof Player;
        //            }
        //
        //            if (!(sender instanceof Player player)) {
        //                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
        //                return false;
        //            }
        //
        //            Optional<? extends Scene<?>> sceneOptional = sceneMapper.apply(player.getUuid());
        //            if (sceneOptional.isEmpty()) {
        //                sender.sendMessage(Component.text("You have to be in a scene!", NamedTextColor.RED));
        //                return false;
        //            }
        //
        //            Party party = partyMap.get(player.getUuid());
        //            if (party == null) {
        //                sender.sendMessage(Component.text("You have to be in a party!", NamedTextColor.GREEN));
        //                return false;
        //            }
        //
        //            PartyMember member = party.getMemberManager().getMember(player.getUuid());
        //            if (!party.getJoinPermission().hasPermission(member)) {
        //                sender.sendMessage(Component.text("You don't have permission to warp players!", NamedTextColor.RED));
        //                return false;
        //            }
        //
        //            return true;
        //        }, (sender, context) -> {
        //            Player joiner = (Player)sender;
        //
        //            sceneMapper.apply(joiner.getUuid()).ifPresent(scene -> {
        //                Party party = partyMap.get(joiner.getUuid());
        //                Collection<PlayerView> playerViews;
        //                if (party == null) {
        //                    playerViews = Collections.singleton(viewProvider.fromPlayer(joiner));
        //                }
        //                else {
        //                    playerViews = new ArrayList<>(party.getMemberManager().getMembers().size());
        //                    for (GuildMember guildMember : party.getMemberManager().getMembers().values()) {
        //                        playerViews.add(guildMember.getPlayerView());
        //                    }
        //                }
        //
        //                transferHelper.transfer(scene);
        //            });
        //        });

        return command;
    }

}
