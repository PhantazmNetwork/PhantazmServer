package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record RouteResult(@NotNull Optional<TransferResult> result,
                          @NotNull Optional<Component> message) {

    public static RouteResult success(@NotNull TransferResult scene) {
        return new RouteResult(Optional.of(scene), Optional.empty());
    }

    public static RouteResult failure(@NotNull Component message) {
        return new RouteResult(Optional.empty(), Optional.of(message));
    }

    public RouteResult {
        Objects.requireNonNull(result, "scene");
        Objects.requireNonNull(message, "message");
    }

}
