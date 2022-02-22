package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record JoinResult(boolean success, @NotNull Optional<String> message) {

    public JoinResult {
        Objects.requireNonNull(message, "message");
    }

}
