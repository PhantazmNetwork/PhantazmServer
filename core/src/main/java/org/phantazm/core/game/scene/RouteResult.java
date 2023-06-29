package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record RouteResult<TScene extends Scene<?>>(@NotNull Optional<CompletableFuture<TScene>> sceneFuture,
                                                   @NotNull Optional<Component> message) {

    public static <TScene extends Scene<?>> RouteResult<TScene> success(@NotNull CompletableFuture<TScene> sceneFuture) {
        return new RouteResult<>(Optional.of(sceneFuture), Optional.empty());
    }

    public static <TScene extends Scene<?>> RouteResult<TScene> failure(@NotNull Component message) {
        return new RouteResult<>(Optional.empty(), Optional.of(message));
    }

    public RouteResult {
        Objects.requireNonNull(sceneFuture, "sceneFuture");
        Objects.requireNonNull(message, "message");
    }

}
