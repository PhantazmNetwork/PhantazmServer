package com.github.zapv3.server.config.server;

import org.jetbrains.annotations.NotNull;

/**
 * Config for server info
 * @param serverIP The IP to run the server on
 * @param port The port to run the server on
 * @param mojangAuthEnabled Whether the server will use mojang's authentication
 * @param optifineEnabled Whether optifine support is enabled
 */
public record ServerInfoConfig(@NotNull String serverIP, int port, boolean mojangAuthEnabled, boolean optifineEnabled) {

}
