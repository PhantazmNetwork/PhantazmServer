package com.github.phantazmnetwork.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of routing in a {@link Scene}.
 */
public record RouteResult(boolean success, @NotNull Optional<Component> message) {

    /**
     * Creates a route result.
     *
     * @param success Whether the routing was successful
     * @param message A message relating to the routing
     */
    public RouteResult {
        Objects.requireNonNull(message, "message");
    }

}
