package com.github.phantazmnetwork.api.game.scene.router;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SceneRouterKey<TDispatcher extends SceneRouter<?>>(@NotNull String key) {

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
