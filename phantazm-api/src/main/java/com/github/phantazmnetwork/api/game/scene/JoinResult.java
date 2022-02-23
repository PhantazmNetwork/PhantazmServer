package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of joining a {@link Scene}.
 * @param success Whether the join was successful
 * @param message A message relating to the join
 */
public record JoinResult(boolean success, @NotNull Optional<String> message) {

    public JoinResult {
        Objects.requireNonNull(message, "message");
    }

}
