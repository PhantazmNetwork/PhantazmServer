package org.phantazm.server.config.server;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General config for the server.
 *
 * @param serverInfoConfig Config for the server's setup
 * @param pingListConfig   Config for the server's ping list
 */
public record ServerConfig(@NotNull ServerInfoConfig serverInfoConfig,
                           @NotNull PingListConfig pingListConfig,
                           @NotNull Component shutdownMessage) {
    /**
     * The default ServerConfig instance.
     */
    public static final ServerConfig DEFAULT = new ServerConfig(ServerInfoConfig.DEFAULT, PingListConfig.DEFAULT,
            Component.text("The server is shutting down soon!"));

    /**
     * Creates config for the server.
     *
     * @param serverInfoConfig Config for the server's setup
     * @param pingListConfig   Config for the server's ping list
     */
    public ServerConfig {
        Objects.requireNonNull(serverInfoConfig, "serverInfoConfig");
        Objects.requireNonNull(pingListConfig, "pingListConfig");
    }

}
