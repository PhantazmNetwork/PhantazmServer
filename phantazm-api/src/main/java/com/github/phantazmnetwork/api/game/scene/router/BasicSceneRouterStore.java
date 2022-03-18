package com.github.phantazmnetwork.api.game.scene.router;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BasicSceneRouterStore implements SceneRouterStore {

    private final Map<SceneRouterKey<?>, SceneRouter<?>> sceneRouters = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends SceneRouter<?>> @NotNull T getSceneRouter(@NotNull SceneRouterKey<T> key) {
        return (T) sceneRouters.get(key);
    }

    @Override
    public <T extends SceneRouter<?>> void addSceneRouter(@NotNull SceneRouterKey<T> key, @NotNull T router) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(router, "router");

        sceneRouters.put(key, router);
    }

}
