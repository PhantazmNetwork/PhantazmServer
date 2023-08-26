package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.game.scene.fallback.CompositeFallback;
import org.phantazm.core.game.scene.fallback.KickFallback;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.game.scene.lobby.*;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.lobby.LobbyConfig;
import org.phantazm.server.role.RoleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
     * @param playerViewProvider the {@link PlayerViewProvider} instance used by the server
     * @param lobbiesConfig      the {@link LobbiesConfig} used to determine lobby behavior
     */
    static void initialize(@NotNull PlayerViewProvider playerViewProvider, @NotNull LobbiesConfig lobbiesConfig,
        @NotNull ContextManager contextManager, @NotNull RoleStore roleStore) {
        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        try {
            FileUtils.createDirectories(lobbiesConfig.instancesPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InstanceLoader instanceLoader =
            new AnvilFileSystemInstanceLoader(instanceManager, lobbiesConfig.instancesPath(), DynamicChunk::new,
                ExecutorFeature.getExecutor());
        SceneFallback finalFallback = new KickFallback(lobbiesConfig.kickMessage());

        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> lobbyProviders =
            new HashMap<>(lobbiesConfig.lobbies().size());
        lobbyRouter = new LobbyRouter(lobbyProviders);

        LobbyConfig mainLobbyConfig = lobbiesConfig.lobbies().get(lobbiesConfig.mainLobbyName());
        if (mainLobbyConfig == null) {
            throw new IllegalArgumentException("No main lobby config present");
        }

        LOGGER.info("Preloading {} lobby instances", lobbiesConfig.lobbies().size());
        for (LobbyConfig lobbyConfig : lobbiesConfig.lobbies().values()) {
            instanceLoader.preload(lobbyConfig.lobbyPaths(), lobbyConfig.instanceConfig().spawnPoint(),
                lobbyConfig.instanceConfig().chunkLoadDistance());
        }

        Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler = (player -> {
            return roleStore.getStylingRole(player.getUuid()).whenComplete((result, error) -> {
                if (error != null) {
                    return;
                }

                result.styleDisplayName(player);
            });
        });

        SceneProvider<Lobby, LobbyJoinRequest> mainLobbyProvider =
            new BasicLobbyProvider(ExecutorFeature.getExecutor(), mainLobbyConfig.maxLobbies(),
                -mainLobbyConfig.maxPlayers(), instanceLoader, mainLobbyConfig.lobbyPaths(), finalFallback,
                mainLobbyConfig.instanceConfig(), contextManager, mainLobbyConfig.npcs(),
                mainLobbyConfig.defaultItems(), MiniMessage.miniMessage(), mainLobbyConfig.lobbyJoinFormat(),
                false, node, playerViewProvider, displayNameStyler);
        lobbyProviders.put(lobbiesConfig.mainLobbyName(), mainLobbyProvider);

        fallback = new LobbyRouterFallback(LobbyFeature.getLobbyRouter(), lobbiesConfig.mainLobbyName());
        SceneFallback regularFallback = new CompositeFallback(List.of(fallback, finalFallback));

        for (Map.Entry<String, LobbyConfig> lobby : lobbiesConfig.lobbies().entrySet()) {
            if (!lobby.getKey().equals(lobbiesConfig.mainLobbyName())) {
                lobbyProviders.put(lobby.getKey(),
                    new BasicLobbyProvider(ExecutorFeature.getExecutor(), lobby.getValue().maxLobbies(),
                        -lobby.getValue().maxPlayers(), instanceLoader, lobby.getValue().lobbyPaths(),
                        regularFallback, lobby.getValue().instanceConfig(), contextManager,
                        mainLobbyConfig.npcs(), mainLobbyConfig.defaultItems(), MiniMessage.miniMessage(),
                        lobby.getValue().lobbyJoinFormat(), true, node, playerViewProvider, displayNameStyler));
            }
        }

        Map<UUID, LoginLobbyJoinRequest> loginJoinRequests = new HashMap<>();

        node.addListener(PlayerLoginEvent.class, event -> {
            LoginLobbyJoinRequest joinRequest = new LoginLobbyJoinRequest(event, playerViewProvider);
            LobbyRouteRequest routeRequest = new LobbyRouteRequest(lobbiesConfig.mainLobbyName(), joinRequest);

            Player player = event.getPlayer();
            RouteResult routeResult = lobbyRouter.findScene(routeRequest).join();
            boolean success = false;
            if (routeResult.result().isPresent()) {
                try (TransferResult transferResult = routeResult.result().get()) {
                    if (transferResult.executor().isPresent()) {
                        transferResult.executor().get().run();
                        loginJoinRequests.put(player.getUuid(), joinRequest);
                        success = true;
                    }
                }
            }

            if (!success) {
                LOGGER.warn("Kicking player {} because we weren't able to find a lobby for them to join",
                    player.getUuid());
                event.setCancelled(true);
            }
        });

        node.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) {
                return;
            }

            Player player = event.getPlayer();
            LoginLobbyJoinRequest joinRequest = loginJoinRequests.remove(player.getUuid());
            if (joinRequest == null) {
                LOGGER.warn("Player {} spawned without a login join request", player.getUuid());
                player.kick(Component.empty());
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
