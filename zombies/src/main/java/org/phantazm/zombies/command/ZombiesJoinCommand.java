package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene.ZombiesJoinHelper;

import java.util.*;

public class ZombiesJoinCommand extends Command {
    public ZombiesJoinCommand(@NotNull Map<? super UUID, ? extends Party> partyMap,
            @NotNull PlayerViewProvider viewProvider, @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps,
            @NotNull ZombiesJoinHelper joinHelper) {
        super("join");

        Argument<String> mapKeyArgument = ArgumentType.String("map-key");

        Objects.requireNonNull(partyMap, "partyMap");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(maps, "maps");
        Objects.requireNonNull(joinHelper, "joinHelper");

        mapKeyArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (Map.Entry<Key, MapInfo> entry : maps.entrySet()) {
                suggestion.addEntry(
                        new SuggestionEntry(entry.getKey().asString(), entry.getValue().settings().displayName()));
            }
        });
        addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party != null) {
                PartyMember member = party.getMemberManager().getMember(player.getUuid());
                if (!party.getJoinPermission().hasPermission(member)) {
                    sender.sendMessage(Component.text("You don't have permission in your party to join games!",
                            NamedTextColor.RED));
                    return false;
                }
            }

            return true;
        }, (sender, context) -> {
            @Subst("test_map")
            String mapKeyString = context.get(mapKeyArgument);
            if (!keyParser.isValidKey(mapKeyString)) {
                sender.sendMessage(Component.text("Invalid key!", NamedTextColor.RED));
                return;
            }

            Key targetMap = keyParser.parseKey(mapKeyString);
            if (!maps.containsKey(targetMap)) {
                sender.sendMessage(Component.text("Invalid map!", NamedTextColor.RED));
                return;
            }

            Player joiner = (Player)sender;
            Party party = partyMap.get(joiner.getUuid());
            Collection<PlayerView> playerViews;
            if (party == null) {
                playerViews = Collections.singleton(viewProvider.fromPlayer(joiner));
            }
            else {
                playerViews = new ArrayList<>(party.getMemberManager().getMembers().size());
                for (GuildMember guildMember : party.getMemberManager().getMembers().values()) {
                    playerViews.add(guildMember.getPlayerView());
                }
            }

            joinHelper.joinGame(joiner, playerViews, targetMap, party != null);
        }, mapKeyArgument);
    }

}
