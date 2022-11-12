package com.github.phantazmnetwork.zombies.scene;

import com.github.phantazmnetwork.core.game.scene.RouteResult;
import com.github.phantazmnetwork.core.game.scene.Scene;
import com.github.phantazmnetwork.core.game.scene.SceneProvider;
import com.github.phantazmnetwork.core.player.PlayerView;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

// TODO: almost everything is copied from LobbySceneRouter, but trying to create a base creates a lot of generics
public class ZombiesSceneRouter implements Scene<ZombiesRouteRequest> {

    private final Map<Key, SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders;

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

    public ZombiesSceneRouter(@NotNull Map<Key, SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders) {
        this.sceneProviders = Objects.requireNonNull(sceneProviders, "sceneProviders");
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

        SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider = sceneProviders.get(routeRequest.targetMap());
        if (sceneProvider == null) {
            return new RouteResult(false, Component.text("No games exist with key " + routeRequest.targetMap() + "."));
        }

        ZombiesJoinRequest joinRequest = routeRequest.joinRequest();
        Optional<ZombiesScene> sceneOptional = sceneProvider.provideScene(joinRequest);
        if (sceneOptional.isPresent()) {
            ZombiesScene scene = sceneOptional.get();
            RouteResult result = scene.join(joinRequest);
            if (result.success()) {
                for (PlayerView playerView : routeRequest.joinRequest().getPlayers()) {
                    ZombiesScene oldScene = playerSceneMap.get(playerView.getUUID());
                    if (oldScene != null && oldScene != scene) {
                        oldScene.leave(Collections.singleton(playerView.getUUID()));
                    }

                    playerSceneMap.put(playerView.getUUID(), scene);
                }
            }

            return result;
        }

        return new RouteResult(false, Component.text("No games are joinable."));
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID uuid : leavers) {
            if (!playerSceneMap.containsKey(uuid)) {
                return new RouteResult(false, Component.text(uuid + " is not part of a game in the Zombies router."));
            }
        }

        for (UUID uuid : leavers) {
            playerSceneMap.get(uuid).leave(Collections.singleton(uuid));
        }

        return RouteResult.SUCCESSFUL;
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        return unmodifiablePlayers;
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
    public void forceShutdown() {
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
}
