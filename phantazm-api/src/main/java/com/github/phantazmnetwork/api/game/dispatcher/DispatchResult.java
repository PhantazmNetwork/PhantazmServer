package com.github.phantazmnetwork.api.game.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of dispatching in a {@link SceneDispatcher}.
 * @param success Whether the dispatch was successful
 * @param message A message relating to the dispatch
 */
public record DispatchResult(boolean success, @NotNull Optional<String> message) {

    public DispatchResult {
        Objects.requireNonNull(message, "message");
    }

}
