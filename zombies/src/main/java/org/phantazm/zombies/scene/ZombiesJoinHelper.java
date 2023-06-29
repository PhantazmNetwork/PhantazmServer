package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.*;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ZombiesJoinHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesJoinHelper.class);

    private final PlayerViewProvider viewProvider;

    private final SceneRouter<ZombiesScene, ZombiesRouteRequest> router;

    private final SceneTransferHelper transferHelper;

    public ZombiesJoinHelper(@NotNull PlayerViewProvider viewProvider,
            @NotNull SceneRouter<ZombiesScene, ZombiesRouteRequest> router,
            @NotNull SceneTransferHelper transferHelper) {
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.router = Objects.requireNonNull(router, "router");
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
        ZombiesJoinRequest joinRequest = () -> playerViews;
        RouteResult<ZombiesScene> result = router.findScene(routeRequestCreator.apply(joinRequest));

        if (result.message().isPresent()) {
            joiner.sendMessage(result.message().get());
        }
        else if (result.sceneFuture().isPresent()) {
            CompletableFuture<ZombiesScene> sceneFuture = result.sceneFuture().get();
            sceneFuture.whenComplete((scene, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Exception while loading zombies scene", throwable);
                    return;
                }

                transferHelper.transfer(scene, joinRequest, playerViews, viewProvider.fromPlayer(joiner));
            });
        }
    }

}
