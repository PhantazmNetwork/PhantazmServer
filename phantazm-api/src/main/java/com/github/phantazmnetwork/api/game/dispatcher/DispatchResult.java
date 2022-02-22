package com.github.phantazmnetwork.api.game.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record DispatchResult(boolean success, @NotNull Optional<String> message) {

    public DispatchResult {
        Objects.requireNonNull(message, "message");
    }

}
