package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.Namespaces;
import org.phantazm.core.game.scene.fallback.CompositeFallback;
import org.phantazm.core.game.scene.fallback.KickFallback;
import org.phantazm.core.game.scene.lobby.LobbyRouterFallback;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.server.PathfinderConfig;
import org.phantazm.server.config.server.ServerConfig;
import org.phantazm.server.config.server.ServerInfoConfig;
import org.phantazm.zombies.equipment.EquipmentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Launches the server, and provides some useful static constants.
 */
public final class PhantazmServer {
    /**
     * The default, global {@link Logger} for PhantazmServer.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PhantazmServer.class);
    public static final String BRAND_NAME = "Minestom-Phantazm";
    private static final String UNSAFE_ARGUMENT = "unsafe";

    /**
     * Starting point for the server.
     *
     * @param args Do you even know java?
     *             I don't know java.
     *             At all.
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        ServerConfig serverConfig;
        LobbiesConfig lobbiesConfig;
        PathfinderConfig pathfinderConfig;
        try {
            LOGGER.info("Loading server configuration data.");
            Config.initialize();
            ConfigHandler handler = Config.getHandler();
            handler.writeDefaultsAndGet();

            serverConfig = handler.loadDataNow(Config.SERVER_CONFIG_KEY);
            ServerInfoConfig serverInfoConfig = serverConfig.serverInfoConfig();
            if (isUnsafe(args)) {
                LOGGER.warn("""
                                                
                                                ██
                                              ██░░██
                                            ██░░░░░░██
                                          ██░░░░░░░░░░██
                                          ██░░░░░░░░░░██
                                        ██░░░░░░░░░░░░░░██
                                      ██░░░░░░██████░░░░░░██
                                      ██░░░░░░██████░░░░░░██
                                    ██░░░░░░░░██████░░░░░░░░██
                                    ██░░░░░░░░██████░░░░░░░░██
                                  ██░░░░░░░░░░██████░░░░░░░░░░██
                                ██░░░░░░░░░░░░██████░░░░░░░░░░░░██
                                ██░░░░░░░░░░░░██████░░░░░░░░░░░░██
                              ██░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░██
                              ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██
                            ██░░░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░██
                            ██░░░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░██
                          ██░░░░░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░░░██
                          ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██
                            ██████████████████████████████████████████
                        """);
                LOGGER.warn("Server starting in unsafe mode! Your proxy secret may be set to the default value " +
                        "\"default\". Only use this option when running in a secure development environment.");
            }
            else if (serverInfoConfig.isUnsafeConfiguration()) {
                LOGGER.error("When using authType " + serverInfoConfig.authType() + ", proxySecret must be set to a " +
                        "value other than the default for security reasons.");
                LOGGER.error("If you are running in a development environment, you can use the 'unsafe' program " +
                        "argument to force the server to start regardless.");
                shutdown("error during startup");
                return;
            }

            lobbiesConfig = handler.loadDataNow(Config.LOBBIES_CONFIG_KEY);
            pathfinderConfig = handler.loadDataNow(Config.PATHFINDER_CONFIG_KEY);
            LOGGER.info("Server configuration loaded successfully.");
        }
        catch (ConfigProcessException e) {
            LOGGER.error("Fatal error when loading configuration data", e);
            shutdown("error during startup");
            return;
        }

        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();
        try {
            LOGGER.info("Initializing features.");
            initializeFeatures(node, serverConfig, pathfinderConfig, lobbiesConfig);
            LOGGER.info("Features initialized successfully.");
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during initialization", exception);
            shutdown("error during startup");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!MinecraftServer.isStopping()) {
                shutdown("interrupt");
            }
        }));

        MinecraftServer.setBrandName(BRAND_NAME);

        try {
            startServer(node, minecraftServer, serverConfig);
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during server startup", exception);
            shutdown("error during startup");
        }
    }

    public static void shutdown(@Nullable String reason) {
        LOGGER.info("Shutting down server. Reason: " + reason);
        MinecraftServer.stopCleanly();
    }

    private static boolean isUnsafe(String[] args) {
        for (String arg : args) {
            if (arg.equals(UNSAFE_ARGUMENT)) {
                return true;
            }
        }

        return false;
    }

    private static void initializeFeatures(EventNode<Event> global, ServerConfig serverConfig,
            PathfinderConfig pathfinderConfig, LobbiesConfig lobbiesConfig) throws Exception {
        BlockHandlerFeature.initialize(MinecraftServer.getBlockManager());

        KeyParser keyParser = new BasicKeyParser(Namespaces.PHANTAZM);

        SongFeature.initialize(keyParser);
        Ethylene.initialize(keyParser);

        MappingProcessorSource mappingProcessorSource = Ethylene.getMappingProcessorSource();
        Element.initialize(mappingProcessorSource, keyParser);

        ContextManager contextManager = Element.getContextManager();

        TickFormatterFeature.initialize(contextManager);

        PlayerViewProvider viewProvider =
                new BasicPlayerViewProvider(IdentitySource.MOJANG, MinecraftServer.getConnectionManager());

        PartyFeature.initialize(MinecraftServer.getCommandManager(), viewProvider);
        Lobbies.initialize(global, viewProvider, lobbiesConfig);
        Chat.initialize(global, viewProvider, PartyFeature.getParties(), MinecraftServer.getCommandManager());
        Messaging.initialize(global, serverConfig.serverInfoConfig().authType());

        Proxima.initialize(global, contextManager, pathfinderConfig);

        Mob.initialize(contextManager, Path.of("./mobs/"), new YamlCodec());
        EquipmentFeature.initialize(keyParser, contextManager,
                new YamlCodec(() -> new Load(LoadSettings.builder().build()),
                        () -> new Dump(DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build())),
                mappingProcessorSource.processorFor(Token.ofClass(EquipmentData.class)));

        CommandManager commandManager = MinecraftServer.getCommandManager();
        ZombiesFeature.initialize(global, contextManager, Mob.getProcessorMap(), Proxima.getSpawner(), keyParser,
                Proxima.instanceSettingsFunction(), viewProvider, commandManager, new CompositeFallback(
                        List.of(new LobbyRouterFallback(Lobbies.getLobbyRouter(), lobbiesConfig.mainLobbyName()),
                                new KickFallback(Component.text("Failed to send you to lobby", NamedTextColor.RED)))));

        ServerCommandFeature.initialize(commandManager);
    }

    private static void startServer(EventNode<Event> node, MinecraftServer server, ServerConfig serverConfig) {
        ServerInfoConfig infoConfig = serverConfig.serverInfoConfig();

        if (infoConfig.optifineEnabled()) {
            OptifineSupport.enable();
        }

        switch (infoConfig.authType()) {
            case MOJANG -> MojangAuth.init();
            case BUNGEE -> {
                BungeeCordProxy.enable();
                BungeeCordProxy.setBungeeGuardTokens(Set.of(infoConfig.proxySecret()));
            }
            case VELOCITY -> VelocityProxy.enable(infoConfig.proxySecret());
        }

        node.addListener(ServerListPingEvent.class,
                event -> event.getResponseData().setDescription(serverConfig.pingListConfig().description()));

        server.start(infoConfig.serverIP(), infoConfig.port());
        LOGGER.info("serverIP: " + infoConfig.serverIP() + ", port: " + infoConfig.port());
    }
}
