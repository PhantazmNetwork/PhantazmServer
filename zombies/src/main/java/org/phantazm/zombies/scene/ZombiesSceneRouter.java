package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

public class ZombiesSceneRouter implements SceneRouter<ZombiesScene, ZombiesRouteRequest> {
    private final Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders;

    private boolean shutdown = false;
    private boolean joinable = true;

    public ZombiesSceneRouter(
            @NotNull Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders) {
        this.sceneProviders = Objects.requireNonNull(sceneProviders, "sceneProviders");
    }

    @Override
    public @NotNull Optional<ZombiesScene> getCurrentScene(@NotNull UUID uuid) {
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            for (ZombiesScene scene : sceneProvider.getScenes()) {
                ZombiesPlayer player = scene.getZombiesPlayers().get(uuid);

                if (player != null && !player.hasQuit()) {
                    return Optional.of(scene);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Collection<ZombiesScene> getScenesContainingPlayer(@NotNull UUID playerUUID) {
        Collection<ZombiesScene> scenes = new ArrayList<>();
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            for (ZombiesScene scene : sceneProvider.getScenes()) {
                if (scene.getZombiesPlayers().containsKey(playerUUID)) {
                    scenes.add(scene);
                }
            }
        }

        return scenes;
    }

    @Override
    public void tick(long time) {
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            sceneProvider.tick(time);
        }
    }

    @Override
    public @NotNull RouteResult<ZombiesScene> findScene(@NotNull ZombiesRouteRequest routeRequest) {
        if (isShutdown()) {
            return RouteResult.failure(Component.text("The router is shutdown."));
        }
        if (!isJoinable()) {
            return RouteResult.failure(Component.text("The router is not joinable."));
        }

        if (routeRequest.targetMap() != null) {
            return joinGame(routeRequest);
        }

        return rejoinGame(routeRequest);
    }

    private RouteResult<ZombiesScene> joinGame(ZombiesRouteRequest routeRequest) {
        SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider = sceneProviders.get(routeRequest.targetMap());
        if (sceneProvider == null) {
            return RouteResult.failure(Component.text("No games exist with key " + routeRequest.targetMap() + "."));
        }

        return sceneProvider.provideScene(routeRequest.joinRequest()).map(RouteResult::success)
                .orElseGet(() -> RouteResult.failure(Component.text("No games are joinable.")));
    }

    private RouteResult<ZombiesScene> rejoinGame(ZombiesRouteRequest routeRequest) {
        for (ZombiesScene scene : getScenes()) {
            if (scene.getUUID().equals(routeRequest.targetGame())) {
                return RouteResult.success(scene);
            }
        }

        return RouteResult.failure(Component.text("Not a valid game."));
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public boolean isJoinable() {
        return joinable;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public boolean isGame() {
        return true;
    }

    @Override
    public @NotNull Collection<ZombiesScene> getScenes() {
        Collection<ZombiesScene> scenes = new ArrayList<>();
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            scenes.addAll(sceneProvider.getScenes());
        }

        return scenes;
    }

    @Override
    public boolean hasActiveScenes() {
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            if (sceneProvider.hasActiveScenes()) {
                return true;
            }
        }

        return false;
    }
}
