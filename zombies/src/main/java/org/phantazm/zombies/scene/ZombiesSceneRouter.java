package org.phantazm.zombies.scene;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.TransferResult;

import java.util.*;

public class ZombiesSceneRouter implements SceneRouter<ZombiesScene, ZombiesRouteRequest> {
    private final Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders;

    private boolean shutdown = false;
    private boolean joinable = true;

    public ZombiesSceneRouter(@NotNull Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders) {
        this.sceneProviders = Objects.requireNonNull(sceneProviders, "sceneProviders");
    }

    @Override
    public @NotNull Optional<ZombiesScene> getScene(@NotNull UUID uuid) {
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            for (ZombiesScene scene : sceneProvider.getScenes()) {
                if (scene.getPlayers().containsKey(uuid)) {
                    return Optional.of(scene);
                }
            }
        }

        return Optional.empty();
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

    // TODO: optimize
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
    public @NotNull Collection<ZombiesScene> getScenes() {
        Collection<ZombiesScene> scenes = new ArrayList<>();
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            scenes.addAll(sceneProvider.getScenes());
        }

        return scenes;
    }
}
