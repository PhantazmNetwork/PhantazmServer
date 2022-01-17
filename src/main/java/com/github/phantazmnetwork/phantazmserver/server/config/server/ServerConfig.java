package com.github.phantazmnetwork.phantazmserver.server.config.server;

import org.jetbrains.annotations.NotNull;

/**
 * General config for the server
 * @param serverInfoConfig Config for the server's setup
 * @param pingListConfig Config for the server's ping list
 */
public record ServerConfig(@NotNull ServerInfoConfig serverInfoConfig, @NotNull PingListConfig pingListConfig) {

}
