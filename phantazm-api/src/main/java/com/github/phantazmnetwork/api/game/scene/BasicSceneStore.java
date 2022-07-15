package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic implementation of a {@link SceneStore}.
 */
public class BasicSceneStore implements SceneStore {

    private final Map<SceneKey<?>, Scene<?>> scenes = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <TScene extends Scene<?>> @NotNull Optional<TScene> getScene(@NotNull SceneKey<TScene> key) {
        return Optional.ofNullable((TScene)scenes.get(key));
    }

    @Override
    public <TScene extends Scene<?>> void addScene(@NotNull SceneKey<TScene> key, @NotNull TScene scene) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(scene, "scene");

        if (scenes.containsKey(key)) {
            throw new IllegalStateException("Scene already associated with key " + key);
        }
        scenes.put(key, scene);
    }

}
