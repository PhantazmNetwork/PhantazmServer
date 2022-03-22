package com.github.phantazmnetwork.api.game.scene.router;

import com.github.phantazmnetwork.api.game.scene.Scene;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A key for {@link Scene} routers.
 * @param <TRouter> The type of the associated {@link Scene} router
 */
@SuppressWarnings("unused")
public record SceneRouterKey<TRouter extends Scene<?>>(@NotNull String key) {

    /**
     * Creates a {@link Scene} router key.
     * @param key The {@link String} representation of the key
     */
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
