package org.phantazm.core.game.scene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BasicRouterStore implements RouterStore {
    private final Map<RouterKey<?, ?, ?>, SceneRouter<?, ?>> routerMap;

    public BasicRouterStore() {
        this.routerMap = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TScene extends Scene<?>, TRequest extends SceneJoinRequest, TRouter extends SceneRouter<TScene, TRequest>> @NotNull TRouter getRouter(
            @NotNull RouterKey<TScene, TRequest, TRouter> routerKey) {
        TRouter router = (TRouter)routerMap.get(routerKey);
        if (router == null) {
            throw new IllegalArgumentException("No router of type " + routerKey.router().getName());
        }

        return router;
    }

    @Override
    public <TScene extends Scene<?>, TRequest extends SceneJoinRequest, TRouter extends SceneRouter<TScene, TRequest>> void putRouter(
            @NotNull RouterKey<TScene, TRequest, TRouter> key, @NotNull SceneRouter<TScene, TRequest> router) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(router, "router");

        if (routerMap.putIfAbsent(key, router) != null) {
            throw new IllegalArgumentException("A router is already registered to type " + key.router().getName());
        }
    }

    @Override
    public @NotNull @Unmodifiable Collection<SceneRouter<?, ?>> getRouters() {
        return List.copyOf(routerMap.values());
    }

    @Override
    public @NotNull Optional<? extends Scene<?>> getCurrentScene(@NotNull UUID uuid) {
        for (SceneRouter<?, ?> router : routerMap.values()) {
            Optional<? extends Scene<?>> scene = router.getCurrentScene(uuid);
            if (scene.isPresent()) {
                return scene;
            }
        }

        return Optional.empty();
    }
}
