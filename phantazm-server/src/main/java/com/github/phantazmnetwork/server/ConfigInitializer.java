package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.server.config.loader.LobbiesConfigProcessor;
import com.github.phantazmnetwork.server.config.loader.ServerConfigProcessor;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigLoader;
import com.github.steanky.ethylene.core.processor.SyncFileConfigLoader;
import com.moandjiezana.toml.TomlWriter;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;

final class ConfigInitializer {
    private ConfigInitializer() {
        throw new UnsupportedOperationException();
    }

    /**
     * The location of the server configuration file.
     */
    static final Path SERVER_CONFIG_PATH = Path.of("./server-config.toml");

    /**
     * The location of the lobbies configuration file.
     */
    static final Path LOBBIES_CONFIG_PATH = Path.of("./lobbies-config.toml");

    /**
     * The {@link ConfigHandler} instance used to manage {@link ConfigLoader}s.
     */
    static final ConfigHandler CONFIG_HANDLER = new BasicConfigHandler();

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link ServerConfig} loader.
     */
    static final ConfigHandler.ConfigKey<ServerConfig> SERVER_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            ServerConfig.class, "server_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link LobbiesConfig} loader.
     */
    static final ConfigHandler.ConfigKey<LobbiesConfig> LOBBIES_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            LobbiesConfig.class, "lobbies_config");

    void initialize() {
        ConfigCodec codec = new TomlCodec(new TomlWriter.Builder().padArrayDelimitersBy(1).indentValuesBy(4).build());
        MiniMessage miniMessage = MiniMessage.miniMessage();
        CONFIG_HANDLER.registerLoader(SERVER_CONFIG_KEY,
                new SyncFileConfigLoader<>(new ServerConfigProcessor(miniMessage), ServerConfig.DEFAULT,
                        SERVER_CONFIG_PATH, codec));
        CONFIG_HANDLER.registerLoader(LOBBIES_CONFIG_KEY,
                new SyncFileConfigLoader<>(new LobbiesConfigProcessor(miniMessage), LobbiesConfig.DEFAULT,
                        LOBBIES_CONFIG_PATH, codec));
    }
}
