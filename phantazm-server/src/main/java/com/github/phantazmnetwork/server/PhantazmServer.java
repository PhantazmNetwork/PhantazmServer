package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.api.player.IdentitySource;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launches the server, and provides some useful static constants.
 */
public class PhantazmServer {
    /**
     * The default, global {@link Logger} for PhantazmServer.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PhantazmServer.class);

    /**
     * The global EventNode for Phantazm events.
     */
    public static final EventNode<Event> PHANTAZM_EVENT = EventNode.all("phantazm");

    /**
     * Starting point for the server.
     * @param args Do you even know java?
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        ServerConfig serverConfig;
        LobbiesConfig lobbiesConfig;
        try {
            LOGGER.info("Loading configuration data.");
            ConfigInitializer.CONFIG_HANDLER.writeDefaultsAndGet();

            serverConfig = ConfigInitializer.CONFIG_HANDLER.getData(ConfigInitializer.SERVER_CONFIG_KEY);
            lobbiesConfig = ConfigInitializer.CONFIG_HANDLER.getData(ConfigInitializer.LOBBIES_CONFIG_KEY);
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