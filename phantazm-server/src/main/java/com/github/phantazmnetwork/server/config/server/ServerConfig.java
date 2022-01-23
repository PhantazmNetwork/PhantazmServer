package com.github.phantazmnetwork.server.config.server;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General config for the server.
 * @param serverInfoConfig Config for the server's setup
 * @param pingListConfig Config for the server's ping list
 */
public record ServerConfig(@NotNull ServerInfoConfig serverInfoConfig, @NotNull PingListConfig pingListConfig) {

    /**
     * Creates config for the server.
     * @param serverInfoConfig Config for the server's setup
     * @param pingListConfig Config for the server's ping list
     */
    public ServerConfig {
        Objects.requireNonNull(serverInfoConfig, "serverInfoConfig");
        Objects.requireNonNull(pingListConfig, "pingListConfig");
    }

}
