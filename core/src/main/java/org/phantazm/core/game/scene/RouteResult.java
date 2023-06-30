package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record RouteResult<TScene extends Scene<?>>(@NotNull Optional<TScene> scene,
                                                   @NotNull Optional<Component> message) {

    public static <TScene extends Scene<?>> RouteResult<TScene> success(@NotNull TScene scene) {
        return new RouteResult<>(Optional.of(scene), Optional.empty());
    }

    public static <TScene extends Scene<?>> RouteResult<TScene> failure(@NotNull Component message) {
        return new RouteResult<>(Optional.empty(), Optional.of(message));
    }

    public RouteResult {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(message, "message");
    }

}
