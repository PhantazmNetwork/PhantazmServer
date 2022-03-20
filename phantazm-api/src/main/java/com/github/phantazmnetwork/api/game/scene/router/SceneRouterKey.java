package com.github.phantazmnetwork.api.game.scene.router;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A key for {@link SceneRouter}s.
 * @param key The String representation of the key
 * @param <TRouter> The type of the associated {@link SceneRouter}
 */
@SuppressWarnings("unused")
public record SceneRouterKey<TRouter extends SceneRouter<?>>(@NotNull String key) {

    public SceneRouterKey {
        Objects.requireNonNull(key, "key");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SceneRouterKey<?> that = (SceneRouterKey<?>) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

}
