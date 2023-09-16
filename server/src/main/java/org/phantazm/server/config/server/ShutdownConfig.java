package org.phantazm.server.config.server;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record ShutdownConfig(
    @NotNull Component shutdownMessage,
    @NotNull Component forceShutdownMessage,
    long warningInterval,
    long forceShutdownTime,
    long forceShutdownWarningTime) {
    /**
     * The default ShutdownConfig instance.
     */
    public static final ShutdownConfig DEFAULT = new ShutdownConfig(Component.text("The server is shutting down soon!"),
        Component.text("The server will forcefully shut down soon!"), 60000, 1800000L, 1500000L);

    @Default("warningInterval")
    public static @NotNull ConfigElement defaultWarningInterval() {
        return ConfigPrimitive.of(DEFAULT.warningInterval);
    }

    @Default("forceShutdownTime")
    public static @NotNull ConfigElement defaultForceShutdownTime() {
        return ConfigPrimitive.of(DEFAULT.forceShutdownTime);
    }

    @Default("forceShutdownWarningTime")
    public static @NotNull ConfigElement defaultForceShutdownWarningTime() {
        return ConfigPrimitive.of(DEFAULT.forceShutdownWarningTime);
    }
}
