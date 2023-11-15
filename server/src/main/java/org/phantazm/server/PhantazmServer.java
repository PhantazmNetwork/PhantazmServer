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
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.Namespaces;
import org.phantazm.core.chat.ChatConfig;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.CoreJoinKeys;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.event.scene.SceneJoinEvent;
import org.phantazm.core.scene2.lobby.JoinLobby;
import org.phantazm.core.scene2.lobby.Lobby;
import org.phantazm.server.command.whisper.WhisperConfig;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.player.PlayerConfig;
import org.phantazm.server.config.server.*;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.zombies.equipment.EquipmentData;
import org.phantazm.zombies.modifier.ModifierCommandConfig;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
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

    /**
     * Starting point for the server.
     *
     * @param args Do you even know java? I don't know java. At all.
     *             <p>
     *             - Thamid123
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
        JoinReportConfig joinReportConfig;
        ZombiesConfig zombiesConfig;
        ModifierCommandConfig modifierCommandConfig;

        KeyParser keyParser = new BasicKeyParser(Namespaces.PHANTAZM);
        EthyleneFeature.initialize(keyParser);

        try {
            LOGGER.info("Loading server configuration data.");
            ConfigFeature.initialize(EthyleneFeature.getMappingProcessorSource());
            ConfigHandler handler = ConfigFeature.getHandler();
            handler.writeDefaultsAndGet();

            serverConfig = handler.loadDataNow(ConfigFeature.SERVER_CONFIG_KEY);
            ServerInfoConfig serverInfoConfig = serverConfig.serverInfo();
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
            } else if (serverInfoConfig.isUnsafeConfiguration()) {
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
            joinReportConfig = handler.loadDataNow(ConfigFeature.JOIN_REPORT_CONFIG_KEY);
            zombiesConfig = handler.loadDataNow(ConfigFeature.ZOMBIES_CONFIG_KEY);
            modifierCommandConfig = handler.loadDataNow(ConfigFeature.MODIFIER_COMMAND_CONFIG_KEY);
            LOGGER.info("Server configuration loaded successfully.");
        } catch (ConfigProcessException e) {
            LOGGER.error("Fatal error when loading configuration data", e);
            shutdown("error during startup");
            return;
        }

        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();
        try {
            LOGGER.info("Initializing features.");
            initializeFeatures(keyParser, playerConfig, serverConfig, shutdownConfig, pathfinderConfig, lobbiesConfig,
                partyConfig, whisperConfig, chatConfig, joinReportConfig, zombiesConfig, modifierCommandConfig);
            LOGGER.info("Features initialized successfully.");
        } catch (Exception exception) {
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
        } catch (Exception exception) {
            LOGGER.error("Fatal error during server startup", exception);
            shutdown("error during startup");
        }
    }

    public static void shutdown(@Nullable String reason) {
        LOGGER.info("Shutting down server. Reason: " + reason);

        HikariFeature.end();
        ExecutorFeature.shutdown();
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

    private static void initializeFeatures(KeyParser keyParser, PlayerConfig playerConfig, ServerConfig serverConfig,
        ShutdownConfig shutdownConfig, PathfinderConfig pathfinderConfig, LobbiesConfig lobbiesConfig,
        PartyConfig partyConfig, WhisperConfig whisperConfig, ChatConfig chatConfig, JoinReportConfig joinReportConfig,
        ZombiesConfig zombiesConfig, ModifierCommandConfig modifierCommandConfig) {
        ConfigCodec yamlCodec = new YamlCodec(() -> new Load(LoadSettings.builder().build()),
            () -> new Dump(DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build()));
        ConfigCodec tomlCodec = new TomlCodec();

        PlayerViewProvider.Global.init(IdentitySource.MOJANG, MinecraftServer.getConnectionManager(),
            Duration.ofMinutes(2));

        PlayerViewProvider viewProvider = PlayerViewProvider.Global.instance();

        CompletableFuture<?> independentFeatures = CompletableFuture.runAsync(() -> {
            DatapackFeature.initialize();
            WhisperCommandFeature.initialize(whisperConfig);
            SilenceJooqFeature.initialize();
            BlockHandlerFeature.initialize();
            LocalizationFeature.initialize();
            PlayerFeature.initialize(playerConfig);

            SceneManager.Global.init(ExecutorFeature.getExecutor(), Set.of(Lobby.class, ZombiesScene.class),
                viewProvider, Runtime.getRuntime().availableProcessors());

            SceneManager manager = SceneManager.Global.instance();
            manager.registerJoinFunction(CoreJoinKeys.MAIN_LOBBY, SceneManager.joinFunction(Lobby.class, players -> {
                return new JoinLobby(players, LobbyFeature.lobbies().get(lobbiesConfig.mainLobby()).sceneCreator());
            }));

            manager.setLoginHook(player -> {
                return new JoinLobby(Set.of(viewProvider.fromPlayer(player)),
                    LobbyFeature.lobbies().get(lobbiesConfig.mainLobby()).sceneCreator(), true);
            });

            manager.setDefaultFallback(playerViews -> {
                return SceneManager.Global.instance().joinScene(CoreJoinKeys.MAIN_LOBBY, playerViews);
            });

            MinecraftServer.getGlobalEventHandler().addListener(SceneJoinEvent.class, event -> {
                SceneManager.JoinResult<?> result = event.result();
                if (result.successful()) {
                    return;
                }

                PacketGroupingAudience audience = PacketGroupingAudience.of(PlayerView.getMany(event.players(),
                    ArrayList::new));
                switch (result.status()) {
                    case EMPTY_PLAYERS -> audience.sendMessage(joinReportConfig.emptyPlayers());
                    case UNRECOGNIZED_TYPE -> audience.sendMessage(joinReportConfig.unrecognizedType());
                    case ALREADY_JOINING -> audience.sendMessage(joinReportConfig.alreadyJoining());
                    case CANNOT_PROVISION -> audience.sendMessage(joinReportConfig.cannotProvision());
                    case INTERNAL_ERROR -> audience.sendMessage(joinReportConfig.internalError());
                }
            });
        });

        CompletableFuture<?> databaseFeatures = CompletableFuture.runAsync(() -> {
            ExecutorFeature.initialize();
            HikariFeature.initialize();
        });

        CompletableFuture<?> databaseDependents = databaseFeatures.whenCompleteAsync((ignored, error) -> {
            GeneralStatsFeature.initialize(ExecutorFeature.getExecutor(), HikariFeature.getDataSource());
        });

        CompletableFuture<?> game = databaseFeatures.whenCompleteAsync((ignored, error) -> {
            MappingProcessorSource mappingProcessorSource = EthyleneFeature.getMappingProcessorSource();
            ElementFeature.initialize(mappingProcessorSource, keyParser);

            ContextManager contextManager = ElementFeature.getContextManager();

            PartyFeature.initialize(MinecraftServer.getCommandManager(), viewProvider,
                MinecraftServer.getSchedulerManager(), contextManager, partyConfig, tomlCodec);

            RoleFeature.initialize(HikariFeature.getDataSource(), ExecutorFeature.getExecutor(), yamlCodec,
                contextManager);
            ChatFeature.initialize(chatConfig, PartyFeature.getPartyHolder().uuidToGuild(), RoleFeature.roleStore());

            LobbyFeature.initialize(contextManager, RoleFeature.roleStore(),
                ExecutorFeature.getExecutor(), mappingProcessorSource, yamlCodec);

            ProximaFeature.initialize(pathfinderConfig);
            MobFeature.initialize(yamlCodec, mappingProcessorSource, contextManager, ProximaFeature.getPathfinder(),
                ProximaFeature.instanceSettingsFunction());
            EquipmentFeature.initialize(keyParser, contextManager, yamlCodec,
                mappingProcessorSource.processorFor(Token.ofClass(EquipmentData.class)));

            SongFeature.initialize(keyParser);

            ZombiesFeature.initialize(contextManager, keyParser, ProximaFeature.instanceSettingsFunction(), viewProvider,
                PartyFeature.getPartyHolder().uuidToGuild(), SongFeature.songLoader(),
                zombiesConfig, mappingProcessorSource, MobFeature::getMobCreators, modifierCommandConfig);

            LoginValidatorFeature.initialize(HikariFeature.getDataSource(), ExecutorFeature.getExecutor());
            ServerCommandFeature.initialize(LoginValidatorFeature.loginValidator(),
                serverConfig.serverInfo().whitelist(), HikariFeature.getDataSource(),
                ExecutorFeature.getExecutor(), shutdownConfig, zombiesConfig.gamereportConfig(),
                viewProvider, RoleFeature.roleStore());

            ValidationFeature.initialize(LoginValidatorFeature.loginValidator(),
                ServerCommandFeature.permissionHandler());

            CommandFeature.initialize(viewProvider);
        });

        CompletableFuture.allOf(independentFeatures, databaseFeatures, databaseDependents, game).join();
    }

    private static void startServer(EventNode<Event> node, MinecraftServer server, ServerConfig serverConfig,
        StartupConfig startupConfig) {
        ServerInfoConfig infoConfig = serverConfig.serverInfo();

        switch (infoConfig.authType()) {
            case MOJANG -> MojangAuth.init();
            case BUNGEE -> {
                BungeeCordProxy.enable();
                BungeeCordProxy.setBungeeGuardTokens(Set.of(infoConfig.proxySecret()));
            }
            case VELOCITY -> VelocityProxy.enable(infoConfig.proxySecret());
        }

        node.addListener(ServerListPingEvent.class,
            event -> event.getResponseData().setDescription(serverConfig.pingList().description()));

        server.start(infoConfig.serverIP(), infoConfig.port());

        if (startupConfig.hasCommand()) {
            ProcessBuilder processBuilder = new ProcessBuilder(startupConfig.command());
            try {
                processBuilder.start();
            } catch (IOException e) {
                LOGGER.warn("Failed to run startup command", e);
            }
        }

        LOGGER.info("serverIP: " + infoConfig.serverIP() + ", port: " + infoConfig.port());
    }
}
