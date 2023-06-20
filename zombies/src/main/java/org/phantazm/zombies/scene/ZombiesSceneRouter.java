package org.phantazm.zombies.scene;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.stream.Collectors;

public class ZombiesSceneRouter implements SceneRouter<ZombiesScene, ZombiesRouteRequest> {
    private final UUID uuid;
    private final Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders;

    private boolean shutdown = false;
    private boolean joinable = true;

    public ZombiesSceneRouter(@NotNull UUID uuid,
            @NotNull Map<Key, ? extends SceneProvider<ZombiesScene, ZombiesJoinRequest>> sceneProviders) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.sceneProviders = Objects.requireNonNull(sceneProviders, "sceneProviders");
    }

    @Override
    public @NotNull Optional<ZombiesScene> getScene(@NotNull UUID uuid) {
        for (SceneProvider<ZombiesScene, ZombiesJoinRequest> sceneProvider : sceneProviders.values()) {
            for (ZombiesScene scene : sceneProvider.getScenes()) {
                if (scene.getZombiesPlayers().containsKey(uuid)) {
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
        return sceneOptional.map(scene -> scene.join(joinRequest))
                .orElseGet(() -> new RouteResult(false, Component.text("No games are joinable.")));

    }

    // TODO: optimize
    private RouteResult rejoinGame(ZombiesRouteRequest routeRequest) {
        for (ZombiesScene scene : getScenes()) {
            if (!scene.getUUID().equals(routeRequest.targetGame())) {
                continue;
            }

            return scene.join(routeRequest.joinRequest());
        }

        return new RouteResult(false, Component.text("Not a valid game."));
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        List<Pair<ZombiesScene, UUID>> scenes = new ArrayList<>();
        for (UUID uuid : leavers) {
            Optional<ZombiesScene> sceneOptional = getScene(uuid);
            if (sceneOptional.isEmpty()) {
                return new RouteResult(false, Component.text(uuid + " is not part of a game in the Zombies router."));
            }

            scenes.add(Pair.of(sceneOptional.get(), uuid));
        }

        boolean success = true;
        for (Pair<ZombiesScene, UUID> pair : scenes) {
            RouteResult subResult = pair.left().leave(Collections.singleton(pair.right()));

            if (!subResult.success()) {
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
        /*
        Why are we still here? Just to suffer? Every night, I can feel my leg... and my arm... even my fingers. The body
        I've lost… the comrades I've lost... won't stop hurting... It's like they're all still there. You feel it, too,
        don't you?
        */
        return sceneProviders.entrySet().stream().flatMap(keyEntry -> keyEntry.getValue().getScenes().stream())
                .flatMap(scene -> scene.getPlayers().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
