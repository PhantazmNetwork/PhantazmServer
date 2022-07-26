package com.github.phantazmnetwork.server.config.server;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for server settings.
 *
 * @param serverIP        The IP to run the server on
 * @param port            The port to run the server on
 * @param optifineEnabled Whether optifine support is enabled
 * @param authType        The type of authentication the server will use
 * @param proxySecret     The secret used for authentication
 */
public record ServerInfoConfig(@NotNull String serverIP,
                               int port,
                               boolean optifineEnabled,
                               @NotNull AuthType authType,
                               @NotNull String proxySecret) {
    /**
     * The default server address string.
     */
    public static final String DEFAULT_SERVER_ADDRESS = "0.0.0.0";

    /**
     * The default port to bind to.
     */
    public static final int DEFAULT_PORT = 25565;

    /**
     * The default Optifine patch status.
     */
    public static final boolean DEFAULT_OPTIFINE_ENABLED = true;

    /**
     * The default authentication type.
     */
    public static final AuthType DEFAULT_AUTH_TYPE = AuthType.MOJANG;

    /**
     * The default proxy secret. This is used so the config file gets filled in, but the empty string should NEVER
     * be used as an actual secret during production.
     */
    public static final String DEFAULT_PROXY_SECRET = "default";

    /**
     * The default ServerInfoConfig instance.
     */
    public static final ServerInfoConfig DEFAULT =
            new ServerInfoConfig(DEFAULT_SERVER_ADDRESS, DEFAULT_PORT, DEFAULT_OPTIFINE_ENABLED, DEFAULT_AUTH_TYPE,
                    DEFAULT_PROXY_SECRET);

    /**
     * Creates config regarding server settings.
     *
     * @param serverIP        The IP to run the server on
     * @param port            The port to run the server on
     * @param optifineEnabled Whether optifine support is enabled
     * @param authType        The type of authentication the server will use
     * @param proxySecret     The secret used for authentication
     */
    public ServerInfoConfig {
        Objects.requireNonNull(serverIP, "serverIP");
        Objects.requireNonNull(authType, "authType");
        Objects.requireNonNull(proxySecret, "proxySecret");
    }

}
