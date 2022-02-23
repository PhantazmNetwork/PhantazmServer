package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of routing in a {@link Scene}.
 * @param success Whether the routing was successful
 * @param message A message relating to the routing
 */
public record RouteResult(boolean success, @NotNull Optional<String> message) {

    public RouteResult {
        Objects.requireNonNull(message, "message");
    }

}
