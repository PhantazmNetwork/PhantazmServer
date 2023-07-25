package org.phantazm.server.config.server;

import org.jetbrains.annotations.NotNull;

public record StartupConfig(boolean hasCommand, @NotNull String command) {

    public static final StartupConfig DEFAULT = new StartupConfig(false, "");

}
