package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;
import java.util.function.Function;

public class PartyWarpCommand {

    public static @NotNull Command warpCommand(@NotNull Map<? super UUID, ? extends Party> partyMap,
            @NotNull PlayerViewProvider viewProvider,
            @NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper,
            @NotNull SceneTransferHelper transferHelper) {
        Objects.requireNonNull(partyMap, "partyMap");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(sceneMapper, "sceneMapper");
        Objects.requireNonNull(transferHelper, "transferHelper");

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
