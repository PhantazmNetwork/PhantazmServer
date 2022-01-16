package com.github.zapv3.server.config.server;

import org.jetbrains.annotations.NotNull;

public record ServerInfoConfig(@NotNull String serverIP, int port, boolean optifineEnabled) {

}
