package com.github.phantazmnetwork.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.api.chat.ChatChannel;
import com.github.phantazmnetwork.api.chat.command.ChatCommand;
import com.github.phantazmnetwork.api.game.scene.*;
import com.github.phantazmnetwork.api.game.scene.fallback.CompositeFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.KickFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.game.scene.lobby.*;
import com.github.phantazmnetwork.api.instance.AnvilFileSystemInstanceLoader;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import com.github.phantazmnetwork.api.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.api.player.IdentitySource;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.server.config.loader.LobbiesConfigProcessor;
import com.github.phantazmnetwork.server.config.loader.ServerConfigProcessor;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.lobby.LobbyConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigLoader;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.SyncFileConfigLoader;
import com.moandjiezana.toml.TomlWriter;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * Launches the server, and provides some useful static constants.
 */
public class PhantazmServer {
    /**
     * The location of the server configuration file.
     */
    public static final Path SERVER_CONFIG_PATH = Path.of("./server-config.toml");

    /**
     * The location of the lobbies configuration file.
     */
    public static final Path LOBBIES_CONFIG_PATH = Path.of("./lobbies-config.toml");

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
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link LobbiesConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<LobbiesConfig> LOBBIES_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
            LobbiesConfig.class, "lobbies_config");

    /**
     * The default {@link ChatChannel} name for players.
     */
    public static final String DEFAULT_CHAT_CHANNEL_NAME = "all";

    /**
     * The default, global {@link Logger} for PhantazmServer.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PhantazmServer.class);

    public static final EventNode<Event> PHANTAZM_EVENT = EventNode.all("phantazm");

    /**
     * Starting point for the server.
     * @param args Do you even know java?
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        ConfigCodec codec = new TomlCodec(new TomlWriter.Builder().padArrayDelimitersBy(1).indentValuesBy(4).build());
        MiniMessage miniMessage = MiniMessage.miniMessage();
        CONFIG_HANDLER.registerLoader(SERVER_CONFIG_KEY,
                new SyncFileConfigLoader<>(new ServerConfigProcessor(miniMessage), ServerConfig.DEFAULT,
                        SERVER_CONFIG_PATH, codec));
        CONFIG_HANDLER.registerLoader(LOBBIES_CONFIG_KEY,
                new SyncFileConfigLoader<>(new LobbiesConfigProcessor(miniMessage), LobbiesConfig.DEFAULT,
                        LOBBIES_CONFIG_PATH, codec));

        ServerConfig serverConfig;
        LobbiesConfig lobbiesConfig;
        try {
            LOGGER.info("Loading configuration data.");
            CONFIG_HANDLER.writeDefaultsAndGet();

            serverConfig = CONFIG_HANDLER.getData(SERVER_CONFIG_KEY);
            lobbiesConfig = CONFIG_HANDLER.getData(LOBBIES_CONFIG_KEY);
            LOGGER.info("Configuration data loaded successfully.");
        }
        catch (ConfigProcessException e) {
            LOGGER.error("Fatal error when loading configuration data", e);
            return;
        }

        try {
            LOGGER.info("Initializing features.");
            initializeFeatures(lobbiesConfig);
            LOGGER.info("Features initialized successfully.");
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during initialization", exception);
        }

        try {
            startServer(minecraftServer, serverConfig);
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during server startup", exception);
        }
    }

    private static void initializeFeatures(LobbiesConfig lobbiesConfig) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(IdentitySource.MOJANG, MinecraftServer
                .getConnectionManager());

        LobbyInitializer.initialize(viewProvider, lobbiesConfig);
        ChatInitializer.initialize(PHANTAZM_EVENT);
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