package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.server.config.loader.LobbiesConfigProcessor;
import com.github.phantazmnetwork.server.config.loader.ServerConfigProcessor;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.SyncFileConfigLoader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Entrypoint for configuration-related features.
 */
public final class Configuration {
    private Configuration() {
        throw new UnsupportedOperationException();
    }

    /**
     * The location of the server configuration file.
     */
    public static final Path SERVER_CONFIG_PATH = Path.of("./server-config.toml");

    /**
     * The location of the lobbies configuration file.
     */
    public static final Path LOBBIES_CONFIG_PATH = Path.of("./lobbies-config.toml");

    private static ConfigHandler handler;

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link ServerConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<ServerConfig> SERVER_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            ServerConfig.class, "server_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link LobbiesConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<LobbiesConfig> LOBBIES_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            LobbiesConfig.class, "lobbies_config");

    /**
     * Initializes server configuration features. Should only be called once from {@link PhantazmServer#main(String[])}.
     */
    static void initialize(String @NotNull[] args) {
        handler = new BasicConfigHandler();

        ConfigCodec codec = new TomlCodec();
        handler.registerLoader(SERVER_CONFIG_KEY, new SyncFileConfigLoader<>(new ServerConfigProcessor(args),
                ServerConfig.DEFAULT, SERVER_CONFIG_PATH, codec));
        handler.registerLoader(LOBBIES_CONFIG_KEY, new SyncFileConfigLoader<>(new LobbiesConfigProcessor(),
                LobbiesConfig.DEFAULT, LOBBIES_CONFIG_PATH, codec));
    }

    /**
     * Returns the {@link ConfigHandler} used by Phantazm.
     * @return the global ConfigHandler
     */
    public static @NotNull ConfigHandler getHandler() {
        if(handler == null) {
            throw new IllegalStateException("Configuration has not been initialized yet");
        }

        return handler;
    }
}
