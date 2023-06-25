package org.phantazm.server.config.server;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record ShutdownConfig(@NotNull Component shutdownMessage,
                             @NotNull Component forceShutdownMessage,
                             long warningInterval,
                             long forceShutdownTime,
                             long forceShutdownWarningTime) {
    /**
     * The default ShutdownConfig instance.
     */
    public static final ShutdownConfig DEFAULT = new ShutdownConfig(Component.text("The server is shutting down soon!"),
            Component.text("The server will forcefully shut down soon!"), 60000, 1800000L, 1500000L);
}
