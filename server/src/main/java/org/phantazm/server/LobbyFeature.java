package org.phantazm.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.game.scene.BasicSceneStore;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneStore;
import org.phantazm.core.game.scene.fallback.CompositeFallback;
import org.phantazm.core.game.scene.fallback.KickFallback;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.game.scene.lobby.*;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.lobby.LobbyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main entrypoint for lobby-related features.
 */
public final class LobbyFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyFeature.class);

    private static LobbyRouter lobbyRouter;

    private static SceneFallback fallback;

    private LobbyFeature() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes lobby-related features. Should only be called once from {@link PhantazmServer#main(String[])}.
     *
     * @param node               the node to register lobby-related events to
     * @param playerViewProvider the {@link PlayerViewProvider} instance used by the server
     * @param lobbiesConfig      the {@link LobbiesConfig} used to determine lobby behavior
     */
    static void initialize(@NotNull EventNode<Event> node, @NotNull PlayerViewProvider playerViewProvider,
            @NotNull LobbiesConfig lobbiesConfig) throws IOException {
        SceneStore sceneStore = new BasicSceneStore();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        FileUtils.createDirectories(lobbiesConfig.instancesPath());
        InstanceLoader instanceLoader =
                new AnvilFileSystemInstanceLoader(instanceManager, lobbiesConfig.instancesPath(), DynamicChunk::new);
        SceneFallback finalFallback = new KickFallback(lobbiesConfig.kickMessage());

        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders =
                new HashMap<>(lobbiesConfig.lobbies().size());
        lobbyRouter = new LobbyRouter(UUID.randomUUID(), lobbyProviders);
        sceneStore.addScene(SceneKeys.LOBBY_ROUTER, lobbyRouter);

        LobbyConfig mainLobbyConfig = lobbiesConfig.lobbies().get(lobbiesConfig.mainLobbyName());
        if (mainLobbyConfig == null) {
            throw new IllegalArgumentException("No main lobby config present");
        }

        LOGGER.info("Preloading {} lobby instances", lobbiesConfig.lobbies().size());
        for (LobbyConfig lobbyConfig : lobbiesConfig.lobbies().values()) {
            instanceLoader.preload(lobbyConfig.lobbyPaths(), lobbyConfig.instanceConfig().spawnPoint(),
                    MinecraftServer.getChunkViewDistance());
        }

        SceneProvider<Lobby, LobbyJoinRequest> mainLobbyProvider =
                new BasicLobbyProvider(mainLobbyConfig.maxLobbies(), -mainLobbyConfig.maxPlayers(), instanceLoader,
                        mainLobbyConfig.lobbyPaths(), finalFallback, mainLobbyConfig.instanceConfig());
        lobbyProviders.put(lobbiesConfig.mainLobbyName(), mainLobbyProvider);

        fallback = new LobbyRouterFallback(MinecraftServer.getConnectionManager(), LobbyFeature.getLobbyRouter(),
                lobbiesConfig.mainLobbyName());
        SceneFallback regularFallback = new CompositeFallback(List.of(fallback, finalFallback));

        for (Map.Entry<String, LobbyConfig> lobby : lobbiesConfig.lobbies().entrySet()) {
            if (!lobby.getKey().equals(lobbiesConfig.mainLobbyName())) {
                lobbyProviders.put(lobby.getKey(),
                        new BasicLobbyProvider(lobby.getValue().maxLobbies(), -lobby.getValue().maxPlayers(),
                                instanceLoader, lobby.getValue().lobbyPaths(), regularFallback,
                                lobby.getValue().instanceConfig()));
            }
        }

        Map<UUID, LoginLobbyJoinRequest> loginJoinRequests = new HashMap<>();

        node.addListener(PlayerLoginEvent.class, event -> {
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

        node.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) {
                return;
            }

            LoginLobbyJoinRequest joinRequest = loginJoinRequests.remove(event.getPlayer().getUuid());
            if (joinRequest == null) {
                LOGGER.warn("Player {} spawned without a login join request", event.getPlayer().getUuid());
                event.getPlayer().kick(Component.empty());
            }
        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            lobbyRouter.tick(System.currentTimeMillis());
        }, TaskSchedule.immediate(), TaskSchedule.nextTick());
    }

    public static @NotNull LobbyRouter getLobbyRouter() {
        return FeatureUtils.check(lobbyRouter);
    }

    public static SceneFallback getFallback() {
        return FeatureUtils.check(fallback);
    }
}
