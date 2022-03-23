package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A key used to identify unique {@link Scene}.
 * @param <TScene> The type of the associated {@link Scene}
 */
@SuppressWarnings("unused")
public record SceneKey<TScene extends Scene<?>>(@NotNull String key) {

    /**
     * Creates a {@link Scene} key.
     * @param key The {@link String} representation of the key
     */
    public SceneKey {
        Objects.requireNonNull(key, "key");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SceneKey<?> that = (SceneKey<?>) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

}
