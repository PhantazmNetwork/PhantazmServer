package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.scene.ZombiesJoinRequest;
import org.phantazm.zombies.scene.ZombiesRouteRequest;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

import java.util.*;
import java.util.function.Function;

public class ZombiesRejoinCommand extends Command {
    public ZombiesRejoinCommand(@NotNull ZombiesSceneRouter router,
            @NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper,
            @NotNull PlayerViewProvider viewProvider, @NotNull Map<? super UUID, ? extends Party> parties) {
        super("rejoin");

        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(sceneMapper, "sceneMapper");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(parties, "parties");

        Argument<UUID> targetGameArgument = ArgumentType.UUID("target-game");
        targetGameArgument.setSuggestionCallback(((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            UUID uuid = player.getUuid();
            for (ZombiesScene scene : router.getScenes()) {
                if (!scene.getZombiesPlayers().containsKey(uuid)) {
                    continue;
                }

                SuggestionEntry entry =
                        new SuggestionEntry(scene.getUUID().toString(), scene.getMapSettingsInfo().displayName());
                suggestion.addEntry(entry);
            }
        }));

        addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID targetGame = context.get(targetGameArgument);

            boolean anyMatch = false;
            for (ZombiesScene scene : router.getScenes()) {
                if (scene.getUUID().equals(targetGame)) {
                    anyMatch = true;
                    break;
                }
            }

            if (!anyMatch) {
                sender.sendMessage(Component.text("Invalid game!", NamedTextColor.RED));
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
            Map<UUID, Scene<?>> previousScenes = new HashMap<>();
            for (PlayerView playerView : playerViews) {
                sceneMapper.apply(playerView.getUUID()).ifPresent(previousScene -> {
                    excluded.add(previousScene.getUUID());
                    previousScenes.put(playerView.getUUID(), previousScene);
                });
            }

            ZombiesJoinRequest joinRequest = new ZombiesJoinRequest() {
                @Override
                public @NotNull Collection<PlayerView> getPlayers() {
                    return playerViews;
                }

                @Override
                public @NotNull Set<UUID> excludedScenes() {
                    return excluded;
                }
            };
            RouteResult<ZombiesScene> result =
                    router.findScene(ZombiesRouteRequest.rejoinGame(targetGame, joinRequest));
            if (result.message().isPresent()) {
                sender.sendMessage(result.message().get());
            }
            else if (result.scene().isPresent()) {
                ZombiesScene scene = result.scene().get();
                boolean anyFailed = false;
                for (PlayerView playerView : playerViews) {
                    Scene<?> oldScene = previousScenes.get(playerView.getUUID());
                    if (oldScene == null || oldScene == scene) {
                        continue;
                    }

                    TransferResult leaveResult = oldScene.leave(Collections.singleton(playerView.getUUID()));
                    if (leaveResult.success()) {
                        continue;
                    }

                    anyFailed = true;
                    leaveResult.message().ifPresent(message -> {
                        playerView.getPlayer().ifPresent(leavingPlayer -> {
                            leavingPlayer.sendMessage(message);
                        });
                    });
                }

                if (anyFailed) {
                    player.sendMessage(
                            Component.text("Failed to join because not all players could leave their old games."));
                }
                else {
                    TransferResult joinResult = scene.join(joinRequest);
                    if (!joinResult.success() && joinResult.message().isPresent()) {
                        player.sendMessage(joinResult.message().get());
                    }
                }
            }
        }, targetGameArgument);
    }
}
