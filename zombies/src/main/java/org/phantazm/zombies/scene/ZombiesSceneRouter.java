package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ZombiesSceneRouter implements SceneRouter<ZombiesScene, ZombiesRouteRequest> {
    public static final Key KEY = Key.key(Namespaces.PHANTAZM, "zombies");

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
    public @NotNull CompletableFuture<RouteResult<ZombiesScene>> findScene(@NotNull ZombiesRouteRequest routeRequest) {
        if (isShutdown()) {
            return CompletableFuture.completedFuture(
                    RouteResult.failure(Component.text("This game has shut down.", NamedTextColor.RED)));
        }
        if (!isJoinable()) {
            return CompletableFuture.completedFuture(
                    RouteResult.failure(Component.text("This game is not " + "joinable.", NamedTextColor.RED)));
        }

        if (routeRequest.targetMap() != null) {
            return joinGame(routeRequest);
        }

        return CompletableFuture.completedFuture(rejoinGame(routeRequest));
    }

    private CompletableFuture<RouteResult<ZombiesScene>> joinGame(ZombiesRouteRequest routeRequest) {
        SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider = sceneProviders.get(routeRequest.targetMap());
        if (sceneProvider == null) {
            return CompletableFuture.completedFuture(RouteResult.failure(
                    Component.text("No games exist with key " + routeRequest.targetMap() + ".", NamedTextColor.RED)));
        }

        return sceneProvider.provideScene(routeRequest.joinRequest()).thenApply(sceneOptional -> {
            return sceneOptional.map(RouteResult::success)
                    .orElseGet(() -> RouteResult.failure(Component.text("No games are joinable.", NamedTextColor.RED)));
        });
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

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
