package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.Scene;
import org.phantazm.core.scene2.SceneManager;

import java.util.*;

public class PartyWarpCommand {

    public static @NotNull Command warpCommand(@NotNull PartyCommandConfig config, @NotNull GuildHolder<Party> partyHolder,
        @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(partyHolder);
        Objects.requireNonNull(viewProvider);

        Command command = new Command("warp");
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
            if (!party.getWarpPermission().hasPermission(member)) {
                sender.sendMessage(config.cannotInvitePlayers());
                return false;
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player) sender;

            // Not inside of condition to avoid any issues with threading
            Optional<Scene> scene = SceneManager.Global.instance().currentScene(viewProvider.fromPlayer(player));
            if (scene.isEmpty()) {
                sender.sendMessage(config.notInScene());
                return;
            }

            Optional<SceneManager.Key<?>> joinKey = scene.get().getDefaultJoinKey();
            if (joinKey.isEmpty()) {
                sender.sendMessage(config.notWarpable());
                return;
            }

            Party party = partyHolder.uuidToGuild().get(player.getUuid());
            GuildMemberManager<PartyMember> memberManager = party.getMemberManager();
            Collection<PartyMember> members = memberManager.getMembers().values();
            Set<PlayerView> playerViews = new HashSet<>(members.size());
            for (PartyMember member : members) {
                playerViews.add(member.getPlayerView());
            }
            SceneManager.Global.instance().joinScene(joinKey.get(), playerViews).thenAccept(result -> {
                party.getNotification().notifyWarp(memberManager.getMember(player.getUuid()), members.size());
            });
        });

        return command;
    }

}
