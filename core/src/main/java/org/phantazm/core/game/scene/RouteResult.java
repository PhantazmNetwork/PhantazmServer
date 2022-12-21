package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of routing in a {@link Scene}.
 */
public record RouteResult(boolean success, @NotNull Optional<Component> message) {
    /**
     * The common successful RouteResult, with an empty message.
     */
    public static final RouteResult SUCCESSFUL = new RouteResult(true, Optional.empty());


    /**
     * Creates a route result.
     *
     * @param success Whether the routing was successful
     * @param message A message relating to the routing
     */
    public RouteResult {
        Objects.requireNonNull(message, "message");
    }

    /**
     * Creates a route result.
     *
     * @param success whether the routing was successful
     * @param message the message, which may be null if none is present
     */
    public RouteResult(boolean success, @Nullable Component message) {
        this(success, Optional.ofNullable(message));
    }
}
