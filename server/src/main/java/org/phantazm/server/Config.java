package org.phantazm.server;

import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.loader.SyncFileConfigLoader;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.config.loader.LobbiesConfigProcessor;
import org.phantazm.server.config.loader.PathfinderConfigProcessor;
import org.phantazm.server.config.loader.ServerConfigProcessor;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.server.PathfinderConfig;
import org.phantazm.server.config.server.ServerConfig;

import java.nio.file.Path;

/**
 * Entrypoint for configuration-related features.
 */
public final class Config {
    /**
     * The location of the server configuration file.
     */
    public static final Path SERVER_CONFIG_PATH = Path.of("./server-config.toml");
    /**
     * The location of the lobbies configuration file.
     */
    public static final Path LOBBIES_CONFIG_PATH = Path.of("./lobbies-config.toml");

    /**
     * The location of the pathfinder configuration file.
     */
    public static final Path PATHFINDER_CONFIG_PATH = Path.of("./pathfinder-config.toml");
    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link ServerConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<ServerConfig> SERVER_CONFIG_KEY =
            new ConfigHandler.ConfigKey<>(ServerConfig.class, "server_config");
    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link LobbiesConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<LobbiesConfig> LOBBIES_CONFIG_KEY =
            new ConfigHandler.ConfigKey<>(LobbiesConfig.class, "lobbies_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link PathfinderConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<PathfinderConfig> PATHFINDER_CONFIG_KEY =
            new ConfigHandler.ConfigKey<>(PathfinderConfig.class, "pathfinder_config");

    private static ConfigHandler handler;

    private Config() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes server configuration features. Should only be called once from {@link PhantazmServer#main(String[])}.
     */
    static void initialize() {
        handler = new BasicConfigHandler();

        ConfigCodec codec = new TomlCodec();
        handler.registerLoader(SERVER_CONFIG_KEY,
                new SyncFileConfigLoader<>(new ServerConfigProcessor(), ServerConfig.DEFAULT, SERVER_CONFIG_PATH,
                        codec));

        handler.registerLoader(LOBBIES_CONFIG_KEY,
                new SyncFileConfigLoader<>(new LobbiesConfigProcessor(), LobbiesConfig.DEFAULT, LOBBIES_CONFIG_PATH,
                        codec));

        handler.registerLoader(PATHFINDER_CONFIG_KEY,
                new SyncFileConfigLoader<>(new PathfinderConfigProcessor(), PathfinderConfig.DEFAULT,
                        PATHFINDER_CONFIG_PATH, codec));
    }

    /**
     * Returns the {@link ConfigHandler} used by Phantazm.
     *
     * @return the global ConfigHandler
     */
    public static @NotNull ConfigHandler getHandler() {
        return FeatureUtils.check(handler);
    }
}
