package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.game.scene.*;
import com.github.phantazmnetwork.api.game.scene.fallback.CompositeFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.KickFallback;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.game.scene.lobby.*;
import com.github.phantazmnetwork.api.instance.AnvilFileSystemInstanceLoader;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.chunk.NeuralChunk;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.lobby.LobbyConfig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class LobbyInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyInitializer.class);

    private LobbyInitializer() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PlayerViewProvider playerViewProvider, @NotNull LobbiesConfig lobbiesConfig) {
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

            LoginLobbyJoinRequest joinRequest = loginJoinRequests.remove(event.getPlayer().getUuid());
            if (joinRequest == null) {
                LOGGER.warn("Player {} spawned without a login join request", event.getPlayer().getUuid());
            }
            else {
                joinRequest.onPlayerLoginComplete();
            }
        });
    }
}