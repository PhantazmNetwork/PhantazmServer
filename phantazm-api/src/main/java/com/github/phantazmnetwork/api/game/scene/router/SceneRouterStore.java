package com.github.phantazmnetwork.api.game.scene.router;

import org.jetbrains.annotations.NotNull;

public interface SceneRouterStore {

    <T extends SceneRouter<?>> @NotNull T getSceneRouter(@NotNull SceneRouterKey<T> key);

    <T extends SceneRouter<?>> void addSceneRouter(@NotNull SceneRouterKey<T> key, @NotNull T router);

}
