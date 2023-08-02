package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.Namespaces;
import org.phantazm.core.chat.ChatConfig;
import org.phantazm.core.game.scene.BasicRouterStore;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.game.scene.fallback.CompositeFallback;
import org.phantazm.core.game.scene.fallback.KickFallback;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.server.command.whisper.WhisperConfig;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.player.PlayerConfig;
import org.phantazm.server.config.server.*;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.server.player.BasicLoginValidator;
import org.phantazm.server.player.LoginValidator;
import org.phantazm.zombies.equipment.EquipmentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

    public static final Path WHITELIST_FILE = Path.of("./whitelist.txt");
    public static final Path BANS_FILE = Path.of("./bans.txt");

    private static LoginValidator loginValidator;

    /**
     * Starting point for the server.
     *
     * @param args Do you even know java?
     *             I don't know java.
     *             At all.
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        PlayerConfig playerConfig;
        ServerConfig serverConfig;
        LobbiesConfig lobbiesConfig;
        PathfinderConfig pathfinderConfig;
        ShutdownConfig shutdownConfig;
        StartupConfig startupConfig;
        PartyConfig partyConfig;
        WhisperConfig whisperConfig;
        ChatConfig chatConfig;
        ZombiesConfig zombiesConfig;

        KeyParser keyParser = new BasicKeyParser(Namespaces.PHANTAZM);
        EthyleneFeature.initialize(keyParser);

        try {
            LOGGER.info("Loading server configuration data.");
            ConfigFeature.initialize(EthyleneFeature.getMappingProcessorSource());
            ConfigHandler handler = ConfigFeature.getHandler();
            handler.writeDefaultsAndGet();

            serverConfig = handler.loadDataNow(ConfigFeature.SERVER_CONFIG_KEY);
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

            playerConfig = handler.loadDataNow(ConfigFeature.PLAYER_CONFIG_KEY);
            lobbiesConfig = handler.loadDataNow(ConfigFeature.LOBBIES_CONFIG_KEY);
            pathfinderConfig = handler.loadDataNow(ConfigFeature.PATHFINDER_CONFIG_KEY);
            shutdownConfig = handler.loadDataNow(ConfigFeature.SHUTDOWN_CONFIG_KEY);
            startupConfig = handler.loadDataNow(ConfigFeature.STARTUP_CONFIG_KEY);
            partyConfig = handler.loadDataNow(ConfigFeature.PARTY_CONFIG_KEY);
            whisperConfig = handler.loadDataNow(ConfigFeature.WHISPER_CONFIG_KEY);
            chatConfig = handler.loadDataNow(ConfigFeature.CHAT_CONFIG_KEY);
            zombiesConfig = handler.loadDataNow(ConfigFeature.ZOMBIES_CONFIG_KEY);
            LOGGER.info("Server configuration loaded successfully.");
        }
        catch (ConfigProcessException e) {
            LOGGER.error("Fatal error when loading configuration data", e);
            shutdown("error during startup");
            return;
        }

        loginValidator =
                new BasicLoginValidator(serverConfig.serverInfoConfig().whitelist(), WHITELIST_FILE, BANS_FILE);

        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();
        try {
            LOGGER.info("Initializing features.");
            initializeFeatures(keyParser, playerConfig, serverConfig, shutdownConfig, pathfinderConfig, lobbiesConfig,
                    partyConfig, whisperConfig, chatConfig, zombiesConfig, loginValidator);
            LOGGER.info("Features initialized successfully.");
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during initialization", exception);
            shutdown("error during startup");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown("interrupt");
        }));

        MinecraftServer.setBrandName(BRAND_NAME);

        try {
            startServer(node, minecraftServer, serverConfig, startupConfig);
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during server startup", exception);
            shutdown("error during startup");
        }
    }

    public static void shutdown(@Nullable String reason) {
        LOGGER.info("Shutting down server. Reason: " + reason);

        HikariFeature.end();
        if (loginValidator != null) {
            loginValidator.flush();
        }

        ExecutorFeature.shutdown();
        MinecraftServer.stopCleanly();
        ServerCommandFeature.flushPermissions();
    }

    private static boolean isUnsafe(String[] args) {
        for (String arg : args) {
            if (arg.equals(UNSAFE_ARGUMENT)) {
                return true;
            }
        }

        return false;
    }

    private static void initializeFeatures(KeyParser keyParser, PlayerConfig playerConfig, ServerConfig serverConfig,
            ShutdownConfig shutdownConfig, PathfinderConfig pathfinderConfig, LobbiesConfig lobbiesConfig,
            PartyConfig partyConfig, WhisperConfig whisperConfig, ChatConfig chatConfig, ZombiesConfig zombiesConfig,
            LoginValidator loginValidator) {
        ConfigCodec yamlCodec = new YamlCodec(() -> new Load(LoadSettings.builder().build()),
                () -> new Dump(DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build()));
        ConfigCodec tomlCodec = new TomlCodec();

        RouterStore routerStore = new BasicRouterStore();
        SceneTransferHelper transferHelper = new SceneTransferHelper(routerStore);
        PlayerViewProvider viewProvider =
                new BasicPlayerViewProvider(IdentitySource.MOJANG, MinecraftServer.getConnectionManager());

        CompletableFuture<?> independentFeatures = CompletableFuture.runAsync(() -> {
            DatapackFeature.initialize();
            WhisperCommandFeature.initialize(whisperConfig);
            SilenceJooqFeature.initialize();
            ExecutorFeature.initialize();
            HikariFeature.initialize();
            GeneralStatsFeature.initialize();
            BlockHandlerFeature.initialize();
            LocalizationFeature.initialize();
            PlayerFeature.initialize(playerConfig);
        });

        CompletableFuture<?> gameFeatures = CompletableFuture.runAsync(() -> {
            MappingProcessorSource mappingProcessorSource = EthyleneFeature.getMappingProcessorSource();
            ElementFeature.initialize(mappingProcessorSource, keyParser);

            ContextManager contextManager = ElementFeature.getContextManager();

            PartyFeature.initialize(MinecraftServer.getCommandManager(), viewProvider,
                    MinecraftServer.getSchedulerManager(), contextManager, partyConfig, tomlCodec);

            LobbyFeature.initialize(viewProvider, lobbiesConfig, contextManager);
            ChatFeature.initialize(viewProvider, chatConfig, PartyFeature.getPartyHolder().uuidToGuild());

            MobFeature.initialize(contextManager, yamlCodec);
            EquipmentFeature.initialize(keyParser, contextManager, yamlCodec,
                    mappingProcessorSource.processorFor(Token.ofClass(EquipmentData.class)));

            ProximaFeature.initialize(pathfinderConfig);
            SongFeature.initialize(keyParser);

            ZombiesFeature.initialize(contextManager, MobFeature.getProcessorMap(), ProximaFeature.getSpawner(),
                    keyParser, ProximaFeature.instanceSettingsFunction(), viewProvider, new CompositeFallback(
                            List.of(LobbyFeature.getFallback(), new KickFallback(
                                    Component.text("Failed to send you to lobby!", NamedTextColor.RED)))),
                    PartyFeature.getPartyHolder().uuidToGuild(), transferHelper, SongFeature.songLoader(),
                    zombiesConfig, EthyleneFeature.getMappingProcessorSource());

            ServerCommandFeature.initialize(loginValidator, serverConfig.serverInfoConfig().whitelist(),
                    mappingProcessorSource, yamlCodec, routerStore, shutdownConfig, zombiesConfig.gamereportConfig(),
                    viewProvider, transferHelper);
            ValidationFeature.initialize(loginValidator, ServerCommandFeature.permissionHandler());

            routerStore.putRouter(RouterKeys.ZOMBIES_SCENE_ROUTER, ZombiesFeature.zombiesSceneRouter());
            routerStore.putRouter(RouterKeys.LOBBY_SCENE_ROUTER, LobbyFeature.getLobbyRouter());

            CommandFeature.initialize(routerStore, viewProvider, LobbyFeature.getFallback());
        });

        CompletableFuture.allOf(independentFeatures, gameFeatures).join();
    }

    private static void startServer(EventNode<Event> node, MinecraftServer server, ServerConfig serverConfig,
            StartupConfig startupConfig) {
        ServerInfoConfig infoConfig = serverConfig.serverInfoConfig();

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

        if (startupConfig.hasCommand()) {
            ProcessBuilder processBuilder = new ProcessBuilder(startupConfig.command());
            try {
                processBuilder.start();
            }
            catch (IOException e) {
                LOGGER.warn("Failed to run startup command", e);
            }
        }

        LOGGER.info("serverIP: " + infoConfig.serverIP() + ", port: " + infoConfig.port());
    }
}
