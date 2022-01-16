package com.github.zapv3.server.config.server;

import org.jetbrains.annotations.NotNull;

public record ServerConfig(@NotNull ServerInfoConfig serverInfoConfig, @NotNull PingListConfig pingListConfig) {

}
