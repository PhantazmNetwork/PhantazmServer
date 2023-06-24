package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.*;
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

    private final SceneTransferHelper transferHelper;

    public ZombiesJoinHelper(@NotNull Map<? super UUID, ? extends Party> parties,
            @NotNull PlayerViewProvider viewProvider, @NotNull SceneRouter<ZombiesScene, ZombiesRouteRequest> router,
            @NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper,
            @NotNull SceneTransferHelper transferHelper) {
        this.parties = Objects.requireNonNull(parties, "parties");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.router = Objects.requireNonNull(router, "router");
        this.sceneMapper = Objects.requireNonNull(sceneMapper, "sceneMapper");
        this.transferHelper = Objects.requireNonNull(transferHelper, "transferHelper");
    }

    public void joinGame(@NotNull Player joiner, @NotNull Collection<PlayerView> playerViews, @NotNull Key targetMap) {
        joinInternal(joiner, playerViews, joinRequest -> ZombiesRouteRequest.joinGame(targetMap, joinRequest));
    }

    public void rejoinGame(@NotNull Player joiner, @NotNull UUID targetGame) {
        joinInternal(joiner, Collections.singleton(viewProvider.fromPlayer(joiner)),
                joinRequest -> ZombiesRouteRequest.rejoinGame(targetGame, joinRequest));
    }

    private void joinInternal(@NotNull Player joiner, @NotNull Collection<PlayerView> playerViews,
            @NotNull Function<ZombiesJoinRequest, ZombiesRouteRequest> routeRequestCreator) {
        Set<UUID> excluded = new HashSet<>();
        for (PlayerView playerView : playerViews) {
            sceneMapper.apply(playerView.getUUID()).ifPresent(previousScene -> {
                excluded.add(previousScene.getUUID());
            });
        }

        ZombiesJoinRequest joinRequest = new ZombiesJoinRequest() {
            @Override
            public @NotNull Collection<PlayerView> getPlayers() {
                return playerViews;
            }
        };
        RouteResult<ZombiesScene> result = router.findScene(routeRequestCreator.apply(joinRequest));

        if (result.message().isPresent()) {
            joiner.sendMessage(result.message().get());
        }
        else if (result.scene().isPresent()) {
            ZombiesScene scene = result.scene().get();
            transferHelper.transfer(scene, joinRequest, playerViews, viewProvider.fromPlayer(joiner));
        }
    }

}
