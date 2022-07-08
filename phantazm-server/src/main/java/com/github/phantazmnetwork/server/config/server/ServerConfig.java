package com.github.phantazmnetwork.server.config.server;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General config for the server.
 * @param serverInfoConfig Config for the server's setup
 * @param pingListConfig Config for the server's ping list
 * @param pathfinderConfig Config for pathfinding
 */
public record ServerConfig(@NotNull ServerInfoConfig serverInfoConfig,
                           @NotNull PingListConfig pingListConfig,
                           @NotNull PathfinderConfig pathfinderConfig) {
    /**
     * The default ServerConfig instance.
     */
    public static final ServerConfig DEFAULT = new ServerConfig(ServerInfoConfig.DEFAULT, PingListConfig.DEFAULT,
            PathfinderConfig.DEFAULT);

    /**
     * Creates config for the server.
     * @param serverInfoConfig Config for the server's setup
     * @param pingListConfig Config for the server's ping list
     * @param pathfinderConfig Config for pathfinding
     */
    public ServerConfig {
        Objects.requireNonNull(serverInfoConfig, "serverInfoConfig");
        Objects.requireNonNull(pingListConfig, "pingListConfig");
        Objects.requireNonNull(pathfinderConfig, "pathfinderConfig");
    }

}
