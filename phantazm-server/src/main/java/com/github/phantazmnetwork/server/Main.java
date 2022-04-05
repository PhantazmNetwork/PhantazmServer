package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.game.scene.*;
import com.github.phantazmnetwork.api.game.scene.fallback.CompositeFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.KickFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.game.scene.lobby.*;
import com.github.phantazmnetwork.api.instance.AnvilFileSystemInstanceLoader;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import com.github.phantazmnetwork.api.player.BasicPlayerView;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.BasicContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.BasicSpawner;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
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
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionManager;
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

            initializeLobbies(lobbiesConfig, logger);
            startServer(minecraftServer, serverConfig);
        }
        catch (ConfigProcessException e) {
            logger.error("Fatal error when loading configuration data", e);
        }
        catch (Exception e) {
            logger.error("Fatal error while starting up server", e);
        }
    }

    private static void initializeLobbies(LobbiesConfig lobbiesConfig, Logger logger) {
        SceneStore sceneStore = new BasicSceneStore();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceLoader instanceLoader = new AnvilFileSystemInstanceLoader(lobbiesConfig.instancesPath());
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
            ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            LoginLobbyJoinRequest joinRequest = new LoginLobbyJoinRequest(event, connectionManager);
            LobbyRouteRequest routeRequest = new LobbyRouteRequest(lobbiesConfig.mainLobbyName(), joinRequest);

            RouteResult result = lobbyRouter.join(routeRequest);
            if (!result.success()) {
                finalFallback.fallback(new BasicPlayerView(connectionManager, event.getPlayer().getUuid()));
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
        Spawner spawner = new BasicSpawner(new BasicContextProvider());
        GroundMinestomDescriptor testDescriptor = new GroundMinestomDescriptor() {
            @Override
            public @NotNull EntityType getEntityType() {
                return EntityType.ZOMBIE;
            }

            @Override
            public float getSpeed() {
                return 1F;
            }

            @Override
            public @NotNull String getID() {
                return "standard_humanoid";
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
                    case "T" -> {
                        GroundNeuralEntity entity = spawner.spawnEntity(instance, player.getPosition().add(5, 0,
                                        0), testDescriptor,
                                GroundNeuralEntity::new);
                        entity.setTarget(player);
                    }
                    case "Z" -> {
                        Pos playerPos = player.getPosition();
                        instance.setBlock(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ(), Block.GOLD_BLOCK);
                    }
                    case "C" -> event.getPlayer().setGameMode(GameMode.CREATIVE);
                }
            }
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
