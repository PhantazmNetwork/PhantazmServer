package com.github.phantazmnetwork.api.game.scene.router;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Store for {@link SceneRouter}s.
 */
public interface SceneRouterStore {

    /**
     * Gets a {@link SceneRouter} by a key.
     * @param key The key associated with the {@link SceneRouter}
     * @param <TRouter> The type of the {@link SceneRouter}
     * @return An {@link Optional} of a {@link SceneRouter}
     */
    <TRouter extends SceneRouter<?>> @NotNull Optional<TRouter> getSceneRouter(@NotNull SceneRouterKey<TRouter> key);

    /**
     * Adds a {@link SceneRouter} to the store
     * @param key The key to associate with the {@link SceneRouter}
     * @param router The {@link SceneRouter} to add
     * @param <TRouter> The type of the {@link SceneRouter}
     * @throws IllegalStateException If a {@link SceneRouter} with the given key has already been added
     */
    <TRouter extends SceneRouter<?>> void addSceneRouter(@NotNull SceneRouterKey<TRouter> key, @NotNull TRouter router);

}
