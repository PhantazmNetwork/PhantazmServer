package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene2.ZombiesJoiner;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ZombiesJoinCommand extends Command {
    public static final Permission BYPASS_SANDBOX_RESTRICTION = new Permission("zombies.playtest.bypass_sandbox");

    @SuppressWarnings("unchecked")
    public ZombiesJoinCommand(@NotNull ZombiesJoiner zombiesJoiner, @NotNull Map<? super UUID, ? extends Party> partyMap,
        @NotNull PlayerViewProvider viewProvider, @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps,
        long ratelimit, @NotNull ZombiesDatabase zombiesDatabase) {
        super("join");

        Argument<String> mapKeyArgument = ArgumentType.Word("map-key");
        Argument<Boolean> restrictedArgument = ArgumentType.Boolean("restricted").setDefaultValue(false);
        Argument<Boolean> sandboxArgument = ArgumentType.Boolean("sandbox").setDefaultValue(false);

        Objects.requireNonNull(partyMap);
        Objects.requireNonNull(viewProvider);
        Objects.requireNonNull(keyParser);
        Objects.requireNonNull(maps);

        Object2LongMap<UUID> lastUsageTimes = new Object2LongOpenHashMap<>();
        mapKeyArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (Map.Entry<Key, MapInfo> entry : maps.entrySet()) {
                suggestion.addEntry(
                    new SuggestionEntry(entry.getKey().asString(), entry.getValue().settings().displayName()));
            }
        });

        restrictedArgument.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("true", Component.text("true")));
            suggestion.addEntry(new SuggestionEntry("false", Component.text("false")));
        });

        sandboxArgument.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("true", Component.text("true")));
            suggestion.addEntry(new SuggestionEntry("false", Component.text("false")));
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
            Player joiner = (Player) sender;
            UUID joinerUUID = joiner.getUuid();
            long currentTime = System.currentTimeMillis();
            if (lastUsageTimes.containsKey(joinerUUID) && currentTime - lastUsageTimes.getLong(joinerUUID) < ratelimit) {
                joiner.sendMessage(Component.text("You're using that command too quickly!", NamedTextColor.RED));
                return;
            } else {
                lastUsageTimes.put(joinerUUID, currentTime);
            }

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

            Party party = partyMap.get(joiner.getUuid());
            Set<PlayerView> playerViews;
            if (party == null) {
                playerViews = Set.of(viewProvider.fromPlayer(joiner));
            } else {
                playerViews = new HashSet<>(party.getMemberManager().getMembers().size());
                for (GuildMember guildMember : party.getMemberManager().getMembers().values()) {
                    playerViews.add(guildMember.getPlayerView());
                }
            }

            boolean sandbox = context.get(sandboxArgument);
            if (sandbox) {
                boolean bypassRestriction = true;
                for (PlayerView playerView : playerViews) {
                    Optional<Player> optional = playerView.getPlayer();
                    if (optional.isEmpty()) {
                        continue;
                    }

                    if (!optional.get().hasPermission(BYPASS_SANDBOX_RESTRICTION)) {
                        bypassRestriction = false;
                        break;
                    }
                }

                if (bypassRestriction) {
                    SceneManager.Global.instance().joinScene(zombiesJoiner.joinSandbox(playerViews, targetMap));
                    return;
                }

                CompletableFuture<Boolean>[] futures = new CompletableFuture[playerViews.size()];
                int i = 0;
                for (PlayerView playerView : playerViews) {
                    futures[i++] = zombiesDatabase.getMapStats(playerView.getUUID(), targetMap).thenApply(stats -> {
                        return stats.getWins() > 0;
                    });
                }

                CompletableFuture.allOf(futures).thenRun(() -> {
                    for (CompletableFuture<Boolean> future : futures) {
                        if (future.isCompletedExceptionally() || !future.join()) {
                            sender.sendMessage(Component.text("All joining members must have beaten at least " +
                                "one game before they can play sandbox mode!", NamedTextColor.RED));
                            return;
                        }
                    }

                    SceneManager.Global.instance().joinScene(zombiesJoiner.joinSandbox(playerViews, targetMap));
                });

                return;
            }

            boolean restricted = context.get(restrictedArgument);
            if (restricted) {
                SceneManager.Global.instance().joinScene(zombiesJoiner.joinRestricted(playerViews, targetMap));
            } else {
                SceneManager.Global.instance().joinScene(zombiesJoiner.joinMap(playerViews, targetMap));
            }
        }, mapKeyArgument, restrictedArgument, sandboxArgument);
    }

}
