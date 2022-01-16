package com.github.zapv3.server.config;

import org.jetbrains.annotations.NotNull;

public record ServerConfig(@NotNull PingListConfig pingListConfig, @NotNull String serverIP, int port,
                           boolean optifineEnabled) {

}
