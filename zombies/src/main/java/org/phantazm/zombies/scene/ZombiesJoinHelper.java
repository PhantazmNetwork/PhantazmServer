package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;
import java.util.function.Function;

public class ZombiesJoinHelper {

    private final Map<? super UUID, ? extends Party> parties;

    private final PlayerViewProvider viewProvider;

    private final SceneRouter<ZombiesScene, ZombiesRouteRequest> router;

    private final Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper;

    public ZombiesJoinHelper(@NotNull Map<? super UUID, ? extends Party> parties,
            @NotNull PlayerViewProvider viewProvider, @NotNull SceneRouter<ZombiesScene, ZombiesRouteRequest> router,
            @NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper) {
        this.parties = Objects.requireNonNull(parties, "parties");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.router = Objects.requireNonNull(router, "router");
        this.sceneMapper = Objects.requireNonNull(sceneMapper, "sceneMapper");
    }

    public void joinGame(@NotNull Player joiner, @NotNull Key targetMap) {
        joinInternal(joiner, joinRequest -> ZombiesRouteRequest.joinGame(targetMap, joinRequest));
    }

    public void rejoinGame(@NotNull Player joiner, @NotNull UUID targetGame) {
        joinInternal(joiner, joinRequest -> ZombiesRouteRequest.rejoinGame(targetGame, joinRequest));
    }

    private void joinInternal(@NotNull Player joiner,
            @NotNull Function<ZombiesJoinRequest, ZombiesRouteRequest> routeRequestCreator) {
        Collection<PlayerView> playerViews;
        Party party = parties.get(joiner.getUuid());
        if (party == null) {
            playerViews = Collections.singleton(viewProvider.fromPlayer(joiner));
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
        RouteResult<ZombiesScene> result = router.findScene(routeRequestCreator.apply(joinRequest));

        if (result.message().isPresent()) {
            joiner.sendMessage(result.message().get());
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
                joiner.sendMessage(
                        Component.text("Failed to join because not all players could leave their old games."));
            }
            else {
                TransferResult joinResult = scene.join(joinRequest);

                if (!joinResult.success() && joinResult.message().isPresent()) {
                    joiner.sendMessage(joinResult.message().get());
                }
            }
        }
    }

}
