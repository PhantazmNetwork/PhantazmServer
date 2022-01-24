package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.instance.FileSystemInstanceLoader;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import com.github.phantazmnetwork.server.config.loader.ServerConfigProcessor;
import com.github.phantazmnetwork.server.config.loader.WorldsConfigProcessor;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.phantazmnetwork.server.config.world.WorldsConfig;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigLoader;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.SyncFileConfigLoader;
import com.moandjiezana.toml.TomlWriter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Launches the server.
 */
public class Main {
    /**
     * The location of the server configuration file.
     */
    public static final Path SERVER_CONFIG_PATH = Paths.get("./server-config.toml");

    /**
     * The location of the world configuration file.
     */
    public static final Path WORLDS_CONFIG_PATH = Paths.get("./worlds-config.toml");

    /**
     * The {@link ConfigHandler} instance used to manage {@link ConfigLoader}s.
     */
    public static final ConfigHandler CONFIG_HANDLER = new BasicConfigHandler();

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link ServerConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<ServerConfig> SERVER_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            ServerConfig.class, "server_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link WorldsConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<WorldsConfig> WORLDS_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            WorldsConfig.class, "worlds_config");

    /**
     * Starting point for the server.
     * @param args Do you even know java?
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        Logger logger = LoggerFactory.getLogger(Main.class);

        ConfigCodec codec = new TomlCodec(new TomlWriter.Builder().padArrayDelimitersBy(1).indentValuesBy(4).build());
        CONFIG_HANDLER.registerLoader(SERVER_CONFIG_KEY,
                new SyncFileConfigLoader<>(new ServerConfigProcessor(MiniMessage.miniMessage()), ServerConfig.DEFAULT,
                        SERVER_CONFIG_PATH, codec));
        CONFIG_HANDLER.registerLoader(WORLDS_CONFIG_KEY, new SyncFileConfigLoader<>(new WorldsConfigProcessor(),
                WorldsConfig.DEFAULT, WORLDS_CONFIG_PATH, codec));

        try {
            CONFIG_HANDLER.writeDefaultsAndGet();

            ServerConfig serverConfig = CONFIG_HANDLER.getData(SERVER_CONFIG_KEY);
            WorldsConfig worldsConfig = CONFIG_HANDLER.getData(WORLDS_CONFIG_KEY);

            initializeWorlds(worldsConfig);
            startServer(minecraftServer, serverConfig);
        } catch (ConfigProcessException e) {
            logger.error("Fatal error when loading configuration data", e);
        }
    }

    private static void initializeWorlds(WorldsConfig worldsConfig) {
        Path worldsPath = worldsConfig.worldsPath();
        InstanceLoader instanceLoader = new FileSystemInstanceLoader(worldsPath, AnvilLoader::new);

        Instance lobby = instanceLoader.loadWorld(MinecraftServer.getInstanceManager(), worldsConfig
                .defaultWorldName());

        //TODO make this handled by lobby framework, not in main class
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(lobby);
            event.getPlayer().setRespawnPoint(worldsConfig.worlds().get(worldsConfig.defaultWorldName()).spawnPoint());
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
    }

    private static void startServer(MinecraftServer server, ServerConfig serverConfig) {
        ServerInfoConfig infoConfig = serverConfig.serverInfoConfig();

        if (infoConfig.optifineEnabled()) {
            OptifineSupport.enable();
        }

        switch (infoConfig.authType()) {
            case MOJANG -> MojangAuth.init();
            case BUNGEE -> BungeeCordProxy.enable();
            case VELOCITY -> VelocityProxy.enable(infoConfig.velocitySecret());
        }

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> event.getResponseData()
                .setDescription(serverConfig.pingListConfig().description()));

        server.start(infoConfig.serverIP(), infoConfig.port());
    }
}
