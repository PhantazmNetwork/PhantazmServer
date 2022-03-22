package com.github.phantazmnetwork.api.game.scene.router;

import com.github.phantazmnetwork.api.game.scene.Scene;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Store for {@link Scene} routers.
 */
public interface SceneRouterStore {

    /**
     * Gets a {@link Scene} routers by a key.
     * @param key The key associated with the {@link Scene} router
     * @param <TRouter> The type of the {@link Scene} router
     * @return An {@link Optional} of a {@link Scene} router
     */
    <TRouter extends Scene<?>> @NotNull Optional<TRouter> getSceneRouter(@NotNull SceneRouterKey<TRouter> key);

    /**
     * Adds a {@link Scene} router to the store
     * @param key The key to associate with the {@link Scene} router
     * @param router The {@link Scene} router to add
     * @param <TRouter> The type of the {@link Scene} router
     * @throws IllegalStateException If a {@link Scene} router with the given key has already been added
     */
    <TRouter extends Scene<?>> void addSceneRouter(@NotNull SceneRouterKey<TRouter> key, @NotNull TRouter router);

}
