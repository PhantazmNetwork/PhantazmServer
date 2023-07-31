package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
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

    public void joinGame(@NotNull Player joiner, @NotNull Collection<PlayerView> playerViews, @NotNull Key targetMap,
            boolean isRestricted) {
        joinInternal(joiner, playerViews, joinRequest -> ZombiesRouteRequest.joinGame(targetMap, joinRequest),
                isRestricted);
    }

    public void rejoinGame(@NotNull Player joiner, @NotNull UUID targetGame) {
        joinInternal(joiner, Collections.singleton(viewProvider.fromPlayer(joiner)),
                joinRequest -> ZombiesRouteRequest.rejoinGame(targetGame, joinRequest), false);
    }

    private void joinInternal(@NotNull Player joiner, @NotNull Collection<PlayerView> playerViews,
            @NotNull Function<ZombiesJoinRequest, ZombiesRouteRequest> routeRequestCreator, boolean isRestricted) {
        UUID uuid = UUID.randomUUID();
        ZombiesJoinRequest joinRequest = new ZombiesJoinRequest() {
            @Override
            public @NotNull Collection<PlayerView> getPlayers() {
                return playerViews;
            }

            @Override
            public @NotNull UUID getUUID() {
                return uuid;
            }

            @Override
            public boolean isRestricted() {
                return isRestricted;
            }
        };
        router.findScene(routeRequestCreator.apply(joinRequest)).whenComplete((routeResult, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while finding zombies scene", throwable);
                return;
            }

            if (routeResult.message().isPresent()) {
                joiner.sendMessage(routeResult.message().get());
            }
            else if (routeResult.result().isPresent()) {
                try (TransferResult transferResult = routeResult.result().get()) {
                    transferHelper.transfer(transferResult, playerViews, viewProvider.fromPlayer(joiner));
                }
            }
        });
    }

}
