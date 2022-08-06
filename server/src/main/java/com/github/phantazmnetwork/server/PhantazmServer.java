package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.core.player.MojangIdentitySource;
import com.github.phantazmnetwork.mob.trigger.MobTriggers;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.server.AuthType;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.steanky.element.core.HashRegistry;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.data.BasicDataIdentifier;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.DataIdentifier;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.element.*;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.BasicKeyExtractor;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
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
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

/**
 * Launches the server, and provides some useful static constants.
 */
public final class PhantazmServer {
    /**
     * The default, global {@link Logger} for PhantazmServer.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PhantazmServer.class);

    /**
     * The global EventNode for Phantazm events.
     */
    public static final EventNode<Event> PHANTAZM_NODE = EventNode.all("phantazm");

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
        try {
            LOGGER.info("Loading server configuration data.");
            Configuration.initialize();
            ConfigHandler handler = Configuration.getHandler();
            handler.writeDefaultsAndGet();

            serverConfig = handler.getData(Configuration.SERVER_CONFIG_KEY);
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
            else if ((serverInfoConfig.authType() == AuthType.VELOCITY ||
                    serverInfoConfig.authType() == AuthType.BUNGEE) &&
                    serverInfoConfig.proxySecret().equals(ServerInfoConfig.DEFAULT_PROXY_SECRET)) {
                LOGGER.error("When using AuthType.VELOCITY or AuthType.BUNGEE, proxySecret must be set to a value " +
                        "other than the default for security reasons.");
                LOGGER.error("If you are running in a development environment, you can use the 'unsafe' program " +
                        "argument to force the server to start regardless.");
                MinecraftServer.stopCleanly();
                return;
            }

            lobbiesConfig = handler.getData(Configuration.LOBBIES_CONFIG_KEY);
            LOGGER.info("Server configuration loaded successfully.");
        }
        catch (ConfigProcessException e) {
            LOGGER.error("Fatal error when loading configuration data", e);
            MinecraftServer.stopCleanly();
            return;
        }

        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();
        try {
            LOGGER.info("Initializing features.");
            initializeFeatures(node, serverConfig, lobbiesConfig);
            LOGGER.info("Features initialized successfully.");
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during initialization", exception);
            MinecraftServer.stopCleanly();
            return;
        }

        try {
            startServer(node, minecraftServer, serverConfig);
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during server startup", exception);
            MinecraftServer.stopCleanly();
        }
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
            LobbiesConfig lobbiesConfig) {
        BasicPlayerViewProvider viewProvider =
                new BasicPlayerViewProvider(new MojangIdentitySource(ForkJoinPool.commonPool()),
                        MinecraftServer.getConnectionManager());

        KeyParser phantazmParser = new BasicKeyParser(Namespaces.PHANTAZM);
        KeyExtractor phantazmExtractor = new BasicKeyExtractor("serialKey", phantazmParser);
        DataInspector phantazmDataInspector = new BasicDataInspector(phantazmParser);
        ElementTypeIdentifier phantazmTypeIdentifier = new BasicElementTypeIdentifier(phantazmParser);
        FactoryResolver phantazmFactoryResolver =
                new BasicFactoryResolver(phantazmParser, phantazmDataInspector, phantazmTypeIdentifier);
        ProcessorResolver phantazmProcessorResolver = new BasicProcessorResolver();
        ElementInspector phantazmInspector =
                new BasicElementInspector(phantazmFactoryResolver, phantazmProcessorResolver);

        DataIdentifier phantazmDataIdentifier = new BasicDataIdentifier(phantazmParser, phantazmTypeIdentifier);
        Registry<ConfigProcessor<?>> processorRegistry = new HashRegistry<>();
        Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        ElementBuilder builder = new BasicElementBuilder(phantazmExtractor, phantazmInspector, phantazmTypeIdentifier,
                phantazmDataIdentifier, processorRegistry, factoryRegistry);

        Lobbies.initialize(global, viewProvider, lobbiesConfig);
        Chat.initialize(global, viewProvider, MinecraftServer.getCommandManager());
        Messaging.initialize(global, viewProvider, serverConfig.serverInfoConfig().authType());
        Neuron.initialize(global, serverConfig.pathfinderConfig());
        NeuronTest.initialize(global, Neuron.getSpawner());
        Mob.initialize(global, Neuron.getSpawner(), MobTriggers.TRIGGERS, Path.of("./mobs/"), new YamlCodec());
        EquipmentFeature.initialize(Path.of("./equipment/"),
                new YamlCodec(() -> new Load(LoadSettings.builder().build()),
                        () -> new Dump(DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build())));
        ZombiesFeature.initialize(builder);
        ZombiesTest.initialize(global);
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
    }
}
