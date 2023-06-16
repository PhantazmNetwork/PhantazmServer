package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.player.PlayerView;

import java.util.*;

public class ZombiesSceneRouter implements SceneRouter<ZombiesScene, ZombiesRouteRequest> {
    private final UUID uuid;
    private final Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders;
    private final Map<UUID, ZombiesScene> playerSceneMap = new HashMap<>();

    private final Map<UUID, PlayerView> unmodifiablePlayers = new AbstractMap<>() {

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof UUID uuid)) {
                return false;
            }

            return playerSceneMap.containsKey(uuid);
        }

        @Override
        public PlayerView get(Object key) {
            if (!(key instanceof UUID uuid)) {
                return null;
            }

            ZombiesScene scene = playerSceneMap.get(uuid);
            if (scene == null) {
                return null;
            }

            return scene.getPlayers().get(uuid);
        }

        @NotNull
        @Override
        public Set<Entry<UUID, PlayerView>> entrySet() {
            Set<Entry<UUID, PlayerView>> entrySet = new HashSet<>();
            for (ZombiesScene scene : playerSceneMap.values()) {
                entrySet.addAll(scene.getPlayers().entrySet());
            }

            return Collections.unmodifiableSet(entrySet);
        }

    };

    private boolean shutdown = false;
    private boolean joinable = true;

    public ZombiesSceneRouter(@NotNull UUID uuid,
            @NotNull Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.sceneProviders = Objects.requireNonNull(sceneProviders, "sceneProviders");
    }

    @Override
    public @NotNull Optional<ZombiesScene> getScene(@NotNull UUID uuid) {
        return Optional.ofNullable(playerSceneMap.get(uuid));
    }

    @Override
    public void tick(long time) {
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            sceneProvider.tick(time);
        }
    }

    @Override
    public @NotNull RouteResult join(@NotNull ZombiesRouteRequest routeRequest) {
        if (isShutdown()) {
            return new RouteResult(false, Component.text("The router is shutdown."));
        }
        if (!isJoinable()) {
            return new RouteResult(false, Component.text("The router is not joinable."));
        }

        if (routeRequest.targetMap() != null) {
            return joinGame(routeRequest);
        }

        return rejoinGame(routeRequest);
    }

    private RouteResult joinGame(ZombiesRouteRequest routeRequest) {
        SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider = sceneProviders.get(routeRequest.targetMap());
        if (sceneProvider == null) {
            return new RouteResult(false, Component.text("No games exist with key " + routeRequest.targetMap() + "."));
        }

        ZombiesJoinRequest joinRequest = routeRequest.joinRequest();
        Optional<ZombiesScene> sceneOptional = sceneProvider.provideScene(joinRequest);
        if (sceneOptional.isEmpty()) {
            return new RouteResult(false, Component.text("No games are joinable."));
        }

        ZombiesScene scene = sceneOptional.get();
        RouteResult result = scene.join(joinRequest);
        if (result.success()) {
            for (PlayerView playerView : routeRequest.joinRequest().getPlayers()) {
                playerSceneMap.put(playerView.getUUID(), scene);
            }
        }

        return result;

    }

    // TODO: optimize
    private RouteResult rejoinGame(ZombiesRouteRequest routeRequest) {
        for (ZombiesScene scene : getScenes()) {
            if (!scene.getUUID().equals(routeRequest.targetGame())) {
                continue;
            }

            RouteResult result = scene.join(routeRequest.joinRequest());
            if (result.success()) {
                for (PlayerView playerView : routeRequest.joinRequest().getPlayers()) {
                    playerSceneMap.put(playerView.getUUID(), scene);
                }
            }

            return result;
        }

        return new RouteResult(false, Component.text("Not a valid game."));
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID uuid : leavers) {
            if (!playerSceneMap.containsKey(uuid)) {
                return new RouteResult(false, Component.text(uuid + " is not part of a game in the Zombies router."));
            }
        }

        boolean success = true;
        for (UUID uuid : leavers) {
            RouteResult subResult = playerSceneMap.get(uuid).leave(Collections.singleton(uuid));
            if (subResult.success()) {
                playerSceneMap.remove(uuid);
            }
            else {
                success = false;
            }
        }

        if (success) {
            return RouteResult.SUCCESSFUL;
        }

        return new RouteResult(false, Optional.of(Component.text("Failed to remove a player from a game.")));
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        return unmodifiablePlayers;
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public int getIngamePlayerCount() {
        int playerCount = 0;

        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            for (ZombiesScene scene : sceneProvider.getScenes()) {
                playerCount += scene.getPlayers().size();
            }
        }

        return playerCount;
    }

    @Override
    public int getJoinWeight(@NotNull ZombiesRouteRequest request) {
        int count = 0;
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            count += sceneProvider.getScenes().size();
        }

        return -count;
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
