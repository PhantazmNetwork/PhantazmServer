package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of transferring a player to or from a {@link Scene}.
 */
public record TransferResult(boolean success, @NotNull Optional<Component> message) {
    /**
     * The common successful {@link TransferResult}, with an empty message.
     */
    public static final TransferResult SUCCESSFUL = new TransferResult(true, Optional.empty());


    /**
     * Creates a transfer result.
     *
     * @param success Whether the routing was successful
     * @param message A message relating to the routing
     */
    public TransferResult {
        Objects.requireNonNull(message, "message");
    }

    /**
     * Creates a route result.
     *
     * @param success whether the routing was successful
     * @param message the message, which may be null if none is present
     */
    public TransferResult(boolean success, @Nullable Component message) {
        this(success, Optional.ofNullable(message));
    }
}
