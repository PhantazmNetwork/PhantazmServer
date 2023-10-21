package org.phantazm.server.config.server;

import org.jetbrains.annotations.NotNull;

public record ServerConfig(@NotNull ServerInfoConfig serverInfo,
    @NotNull PingListConfig pingList) {
    /**
     * The default ServerConfig instance.
     */
    public static final ServerConfig DEFAULT = new ServerConfig(ServerInfoConfig.DEFAULT, PingListConfig.DEFAULT);
}
