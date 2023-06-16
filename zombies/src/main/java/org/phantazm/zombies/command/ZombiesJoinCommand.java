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
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene.ZombiesJoinRequest;
import org.phantazm.zombies.scene.ZombiesRouteRequest;

import java.util.*;
import java.util.function.Function;

public class ZombiesJoinCommand extends Command {
    public ZombiesJoinCommand(@NotNull Scene<ZombiesRouteRequest> router,
            @NotNull Function<? super UUID, Optional<SceneRouter<? extends Scene<?>, ?>>> globalRouterMapper,
            @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps, @NotNull PlayerViewProvider viewProvider,
            @NotNull Map<? super UUID, ? extends Party> parties) {
        super("join");

        Argument<String> mapKeyArgument = ArgumentType.String("map-key");

        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(globalRouterMapper, "globalRouterMapper");
        Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(maps, "maps");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(parties, "parties");

        mapKeyArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (Map.Entry<Key, MapInfo> entry : maps.entrySet()) {
                suggestion.addEntry(
                        new SuggestionEntry(entry.getKey().asString(), entry.getValue().settings().displayName()));
            }
        });
        addConditionalSyntax((sender, commandString) -> {
            if (sender instanceof Player) {
                return true;
            }

            sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
            return false;
        }, (sender, context) -> {
            @Subst("test_map")
            String mapKeyString = context.get(mapKeyArgument);
            if (!keyParser.isValidKey(mapKeyString)) {
                sender.sendMessage(Component.text("Invalid key!", NamedTextColor.RED));
                return;
            }

            Key mapKey = keyParser.parseKey(mapKeyString);
            if (!maps.containsKey(mapKey)) {
                sender.sendMessage(Component.text("Invalid map!", NamedTextColor.RED));
                return;
            }

            Player player = (Player)sender;
            Collection<PlayerView> playerViews;
            Party party = parties.get(player.getUuid());
            if (party == null) {
                playerViews = Collections.singleton(viewProvider.fromPlayer(player));
            }
            else {
                playerViews = new ArrayList<>(party.getMemberManager().getMembers().size());
                for (GuildMember guildMember : party.getMemberManager().getMembers().values()) {
                    playerViews.add(guildMember.getPlayerView());
                }
            }

            Set<UUID> excluded = new HashSet<>();
            for (PlayerView playerView : playerViews) {
                globalRouterMapper.apply(playerView.getUUID()).ifPresent(oldRouter -> {
                    oldRouter.getScene(playerView.getUUID()).map(Scene::getUUID).ifPresent(excluded::add);
                    oldRouter.leave(Collections.singleton(playerView.getUUID()));
                });
            }
            RouteResult result = router.join(ZombiesRouteRequest.joinGame(mapKey, new ZombiesJoinRequest() {
                @Override
                public @NotNull Collection<PlayerView> getPlayers() {
                    return playerViews;
                }

                @Override
                public @NotNull Set<UUID> excludedScenes() {
                    return excluded;
                }
            }));
            result.message().ifPresent(sender::sendMessage);
        }, mapKeyArgument);
    }

}
