package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.game.scene.*;
import com.github.phantazmnetwork.api.chat.BasicChatChannelStore;
import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.api.chat.ChatChannel;
import com.github.phantazmnetwork.api.chat.ChatChannelStore;
import com.github.phantazmnetwork.api.chat.command.ChatCommand;
import com.github.phantazmnetwork.api.game.scene.fallback.CompositeFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.KickFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.game.scene.lobby.*;
import com.github.phantazmnetwork.api.instance.AnvilFileSystemInstanceLoader;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import com.github.phantazmnetwork.api.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.BasicContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.chunk.NeuralChunk;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.ContextualSpawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundNeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.node.Calculator;
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
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

/**
 * Launches the server.
 */
public class Main {
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
     * Starting point for the server.
     * @param args Do you even know java?
     */
    public static void main(String[] args) {
        shuffleThamidsProfile();

        MinecraftServer minecraftServer = MinecraftServer.init();
        Logger logger = LoggerFactory.getLogger(Main.class);

        ConfigCodec codec = new TomlCodec(new TomlWriter.Builder().padArrayDelimitersBy(1).indentValuesBy(4).build());
        MiniMessage miniMessage = MiniMessage.miniMessage();
        CONFIG_HANDLER.registerLoader(SERVER_CONFIG_KEY,
                new SyncFileConfigLoader<>(new ServerConfigProcessor(miniMessage), ServerConfig.DEFAULT,
                        SERVER_CONFIG_PATH, codec));
        CONFIG_HANDLER.registerLoader(LOBBIES_CONFIG_KEY,
                new SyncFileConfigLoader<>(new LobbiesConfigProcessor(miniMessage), LobbiesConfig.DEFAULT,
                        LOBBIES_CONFIG_PATH, codec));

        try {
            CONFIG_HANDLER.writeDefaultsAndGet();

            ServerConfig serverConfig = CONFIG_HANDLER.getData(SERVER_CONFIG_KEY);
            LobbiesConfig lobbiesConfig = CONFIG_HANDLER.getData(LOBBIES_CONFIG_KEY);

            PlayerViewProvider playerViewProvider = new BasicPlayerViewProvider(MinecraftServer.getConnectionManager());
            EventNode<Event> phantazmEventNode = EventNode.all("phantazm-node");

            initializeLobbies(playerViewProvider, lobbiesConfig, logger);
            initializeChat(phantazmEventNode);
            startServer(minecraftServer, serverConfig);
        }
        catch (ConfigProcessException e) {
            logger.error("Fatal error when loading configuration data", e);
        }
        catch (Exception e) {
            logger.error("Fatal error while starting up server", e);
        }
    }

    private static void initializeLobbies(PlayerViewProvider playerViewProvider, LobbiesConfig lobbiesConfig,
                                          Logger logger) {
        SceneStore sceneStore = new BasicSceneStore();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceLoader instanceLoader = new AnvilFileSystemInstanceLoader(lobbiesConfig.instancesPath(),
                NeuralChunk::new);
        SceneFallback finalFallback = new KickFallback(lobbiesConfig.kickMessage());

        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders
                = new HashMap<>(lobbiesConfig.lobbies().size());
        LobbyRouter lobbyRouter = new LobbyRouter(lobbyProviders);
        sceneStore.addScene(SceneKeys.LOBBY_ROUTER, lobbyRouter);

        LobbyConfig mainLobbyConfig = lobbiesConfig.lobbies().get(lobbiesConfig.mainLobbyName());
        if (mainLobbyConfig == null) {
            throw new IllegalArgumentException("No main lobby config present");
        }

        SceneProvider<Lobby, LobbyJoinRequest> mainLobbyProvider = new BasicLobbyProvider(
                mainLobbyConfig.maxLobbies(),
                -mainLobbyConfig.maxPlayers(),
                instanceManager,
                instanceLoader,
                mainLobbyConfig.lobbyPaths(),
                finalFallback,
                mainLobbyConfig.instanceConfig(),
                MinecraftServer.getChunkViewDistance()
        );
        lobbyProviders.put(lobbiesConfig.mainLobbyName(), mainLobbyProvider);

        SceneFallback lobbyFallback = new LobbyRouterFallback(lobbyRouter, lobbiesConfig.mainLobbyName());
        SceneFallback regularFallback = new CompositeFallback(List.of(lobbyFallback, finalFallback));

        for (Map.Entry<String, LobbyConfig> lobby : lobbiesConfig.lobbies().entrySet()) {
            if (!lobby.getKey().equals(lobbiesConfig.mainLobbyName())) {
                lobbyProviders.put(lobby.getKey(), new BasicLobbyProvider(
                        lobby.getValue().maxLobbies(),
                        -lobby.getValue().maxPlayers(),
                        instanceManager,
                        instanceLoader,
                        lobby.getValue().lobbyPaths(),
                        regularFallback,
                        lobby.getValue().instanceConfig(),
                        MinecraftServer.getChunkViewDistance()
                ));
            }
        }

        Map<UUID, LoginLobbyJoinRequest> loginJoinRequests = new HashMap<>();
        EventNode<Event> eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(PlayerLoginEvent.class, event -> {
            LoginLobbyJoinRequest joinRequest = new LoginLobbyJoinRequest(event, playerViewProvider);
            LobbyRouteRequest routeRequest = new LobbyRouteRequest(lobbiesConfig.mainLobbyName(), joinRequest);

            RouteResult result = lobbyRouter.join(routeRequest);
            if (!result.success()) {
                finalFallback.fallback(playerViewProvider.fromPlayer(event.getPlayer()));
            }
            else {
                loginJoinRequests.put(event.getPlayer().getUuid(), joinRequest);
            }
        });

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
           if (!event.isFirstSpawn()) {
               return;
           }

           //TODO remove, for testing
           event.getPlayer().teleport(new Pos(0, 100, 0));
           event.getPlayer().setGameMode(GameMode.CREATIVE);
           event.getPlayer().setFlying(true);
           Pos start = event.getPlayer().getPosition().sub(0, 1, 0);
           for(int i = 0; i < 10; i++) {
               event.getSpawnInstance().setBlock(start.add(i, 0, 0), Block.GOLD_BLOCK);
           }

           LoginLobbyJoinRequest joinRequest = loginJoinRequests.remove(event.getPlayer().getUuid());
           if (joinRequest == null) {
               logger.warn("Player {} spawned without a login join request", event.getPlayer().getUuid());
           }
           else {
               joinRequest.onPlayerLoginComplete();
           }
        });

        //TESTING CODE BELOW
        //TODO remove
        Spawner spawner = new ContextualSpawner(new BasicContextProvider());
        GroundMinestomDescriptor testDescriptor = new GroundMinestomDescriptor() {
            @Override
            public float getStep() {
                return 0.5F;
            }

            @Override
            public @NotNull EntityType getEntityType() {
                return EntityType.PHANTOM;
            }

            @Override
            public @NotNull String getID() {
                return "phantom";
            }

            @Override
            public @NotNull Calculator getCalculator() {
                return Calculator.SQUARED_DISTANCE;
            }

            @Override
            public boolean isComplete(@NotNull Vec3I position, @NotNull Vec3I destination) {
                return position.equals(destination);
            }
        };

        eventNode.addListener(PlayerChatEvent.class, event -> {
            String msg = event.getMessage();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if(instance != null) {
                switch (msg) {
                    case "T" -> spawner.spawnEntity(instance, player.getPosition().add(5, 0, 0), testDescriptor,
                            GroundNeuralEntity::new, neuralEntity -> neuralEntity.getAttribute(Attribute
                                    .MOVEMENT_SPEED).setBaseValue(0.1F)).setTarget(player);
                    case "Z" -> {
                        Pos playerPos = player.getPosition();
                        instance.setBlock(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ(), Block.GOLD_BLOCK);
                    }
                    case "C" -> event.getPlayer().setGameMode(GameMode.CREATIVE);
                    case "ZZ" -> {
                        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
                        creature.setInstance(instance, player.getPosition().add(5, 0, 0));

                        eventNode.addListener(PlayerMoveEvent.class, moveEvent -> creature.getNavigator().setPathTo(
                                moveEvent.getPlayer().getPosition()));
                    }
                }
            }
        });
    }

    private static void initializeChat(EventNode<? super ChatChannelSendEvent> eventNode) {
        ChatChannelStore channelStore = new BasicChatChannelStore("all", (sender, message, messageType, filter) -> {
            if (sender instanceof Entity entity && sender instanceof Identified identified) {
                Instance instance = entity.getInstance();
                if (instance != null) {
                    instance.filterAudience(filter).sendMessage(identified, message, messageType);
                }
            }
        });
        channelStore.registerChannel("self", (sender, message, messageType, filter) -> {
            if (sender != null) {
                Identity identity = (sender instanceof Identified identified)
                        ? identified.identity()
                        : Identity.nil();
                sender.filterAudience(filter).sendMessage(identity, message, messageType);
            }
        });

        Map<UUID, ChatChannel> playerChannels = new HashMap<>();
        MinecraftServer.getCommandManager().register(new ChatCommand(channelStore, playerChannels));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, event -> {
            event.setCancelled(true);

            ChatChannel channel = playerChannels.computeIfAbsent(event.getPlayer().getUuid(),
                    (unused) -> channelStore.getDefaultChannel());
            Component message = (event.getChatFormatFunction() != null)
                    ? event.getChatFormatFunction().apply(event)
                    : event.getDefaultChatFormat().get();

            ChatChannelSendEvent channelSendEvent = new ChatChannelSendEvent(channel, event.getPlayer(),
                    event.getMessage(), message);
            eventNode.callCancellable(channelSendEvent,
                    () -> channel.broadcast(event.getPlayer(), channelSendEvent.getMessage(), MessageType.CHAT,
                            audience -> true));
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

    private static void shuffleThamidsProfile() {
        String profile = "Do people really know how to use Maven? XML makes no sense. I don't know how anyone can " +
                "possibly understand it. Gradle makes so much sense. Groovy DSL is even worse, just use the KOTLIN " +
                "DSL.";
        List<String> list = Arrays.asList(profile.split(" "));
        Collections.shuffle(list);
        System.out.println(Pipe.from(list).reduce((a, b) -> a.concat(" ").concat(b)).orElseThrow());
    }
}
