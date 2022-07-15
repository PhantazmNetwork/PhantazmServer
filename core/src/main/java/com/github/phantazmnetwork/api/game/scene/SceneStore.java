package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Store for {@link Scene}. These {@link Scene}s should be unique and are associated by a key.
 */
public interface SceneStore {

    /**
     * Gets a {@link Scene} by a {@link SceneKey}.
     *
     * @param key      The key associated with the {@link Scene}
     * @param <TScene> The type of the {@link Scene}
     * @return An {@link Optional} of a {@link Scene}
     */
    <TScene extends Scene<?>> @NotNull Optional<TScene> getScene(@NotNull SceneKey<TScene> key);

    /**
     * Adds a {@link Scene} to the store
     *
     * @param key      The key to associate with the {@link Scene}
     * @param scene    The {@link Scene} to add
     * @param <TScene> The type of the {@link Scene}
     * @throws IllegalStateException If a {@link Scene} with the given key has already been added
     */
    <TScene extends Scene<?>> void addScene(@NotNull SceneKey<TScene> key, @NotNull TScene scene);

}
