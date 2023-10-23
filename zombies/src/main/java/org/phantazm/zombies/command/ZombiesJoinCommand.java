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
import org.phantazm.commons.FutureUtils;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.modifier.ModifierHandler;
import org.phantazm.zombies.scene2.ZombiesJoiner;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ZombiesJoinCommand extends Command {
    public static final Permission BYPASS_SANDBOX_RESTRICTION = new Permission("zombies.playtest.bypass_sandbox");

    public ZombiesJoinCommand(@NotNull ZombiesJoiner zombiesJoiner, @NotNull Map<? super UUID, ? extends Party> partyMap,
        @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps, long ratelimit,
        @NotNull ZombiesDatabase zombiesDatabase) {
        super("join");

        Argument<String> mapKeyArgument = ArgumentType.Word("map-key");
        Argument<Boolean> restrictedArgument = ArgumentType.Boolean("restricted").setDefaultValue(false);
        Argument<Boolean> sandboxArgument = ArgumentType.Boolean("sandbox").setDefaultValue(false);

        Objects.requireNonNull(partyMap);
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
            PlayerView joinerView = PlayerViewProvider.Global.instance().fromPlayer(joiner);

            Set<PlayerView> playerViews;
            if (party == null) {
                playerViews = Set.of(joinerView);
            } else {
                playerViews = new HashSet<>(party.getMemberManager().getMembers().size());
                for (GuildMember guildMember : party.getMemberManager().getMembers().values()) {
                    playerViews.add(guildMember.getPlayerView());
                }
            }

            Set<Key> modifiers = ModifierHandler.Global.instance().getModifiers(joinerView);
            MapInfo mapInfo = maps.get(targetMap);

            for (Key modifier : modifiers) {
                if (mapInfo.settings().disallowedModifiers().contains(modifier)) {
                    sender.sendMessage(Component.text("One or more of your modifiers are disallowed on this map!",
                        NamedTextColor.RED));
                    return;
                }
            }

            boolean sandbox = context.get(sandboxArgument);
            boolean restricted = context.get(restrictedArgument);

            if (sandbox || !modifiers.isEmpty()) {
                hasAtLeastOneWin(zombiesDatabase, targetMap, playerViews).whenComplete((result, error) -> {
                    if (result == null || error != null) {
                        return;
                    }

                    if (!result) {
                        sender.sendMessage(Component.text("All joining members must have beaten at least one" +
                            " game before they can play sandbox mode or use modifiers!", NamedTextColor.RED));
                        return;
                    }

                    if (sandbox) {
                        SceneManager.Global.instance().joinScene(zombiesJoiner.joinSandbox(playerViews, targetMap, modifiers));
                        return;
                    }

                    maybeJoinRestricted(zombiesJoiner, playerViews, targetMap, modifiers, restricted);
                });

                return;
            }

            maybeJoinRestricted(zombiesJoiner, playerViews, targetMap, modifiers, restricted);
        }, mapKeyArgument, restrictedArgument, sandboxArgument);
    }

    private void maybeJoinRestricted(ZombiesJoiner zombiesJoiner, Set<PlayerView> playerViews, Key targetMap,
        Set<Key> modifiers, boolean restricted) {
        if (restricted) {
            SceneManager.Global.instance().joinScene(zombiesJoiner.joinRestricted(playerViews, targetMap, modifiers));
        } else {
            SceneManager.Global.instance().joinScene(zombiesJoiner.joinMap(playerViews, targetMap, modifiers));
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Boolean> hasAtLeastOneWin(ZombiesDatabase zombiesDatabase, Key targetMap,
        Collection<PlayerView> playerViews) {
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
            return FutureUtils.trueCompletedFuture();
        }

        CompletableFuture<Boolean>[] futures = new CompletableFuture[playerViews.size()];
        int i = 0;
        for (PlayerView playerView : playerViews) {
            futures[i++] = zombiesDatabase.getMapStats(playerView.getUUID(), targetMap).thenApply(stats -> {
                return stats.getWins() > 0;
            });
        }

        return CompletableFuture.allOf(futures).handle((ignored, error) -> {
            if (error != null) {
                LOGGER.warn("Error checking statistics: ", error);
                return false;
            }

            for (CompletableFuture<Boolean> future : futures) {
                if (future.isCompletedExceptionally() || !future.join()) {
                    return false;
                }
            }

            return true;
        });
    }
}