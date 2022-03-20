package com.github.phantazmnetwork.api.game.scene.router;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic implementation of a {@link SceneRouterStore}.
 */
public class BasicSceneRouterStore implements SceneRouterStore {

    private final Map<SceneRouterKey<?>, SceneRouter<?>> sceneRouters = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <TRouter extends SceneRouter<?>> @NotNull Optional<TRouter> getSceneRouter(@NotNull SceneRouterKey<TRouter> key) {
        return Optional.ofNullable((TRouter) sceneRouters.get(key));
    }

    @Override
    public <TRouter extends SceneRouter<?>> void addSceneRouter(@NotNull SceneRouterKey<TRouter> key, @NotNull TRouter router) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(router, "router");

        if (sceneRouters.containsKey(key)) {
            throw new IllegalStateException("router already associated with key " + key);
        }
        sceneRouters.put(key, router);
    }

}
