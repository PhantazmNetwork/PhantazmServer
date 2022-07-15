package com.github.phantazmnetwork.core.game.scene.lobby;

import com.github.phantazmnetwork.core.config.InstanceConfig;
import com.github.phantazmnetwork.core.game.scene.RouteResult;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.player.PlayerInfo;
import com.github.phantazmnetwork.core.player.PlayerView;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LobbyTest {

    private static final UUID playerUUID = UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127");

    @Test
    public void testShutdown() {
        Instance instance = mock(Instance.class);
        InstanceConfig instanceConfig = new InstanceConfig(InstanceConfig.DEFAULT_POS);
        SceneFallback sceneFallback = (ignored) -> true;
        Lobby lobby = new Lobby(instance, instanceConfig, sceneFallback);
        PlayerView playerView = mock(PlayerView.class);

        lobby.forceShutdown();
        assertTrue(lobby.isShutdown());

        RouteResult result = lobby.join(new BasicLobbyJoinRequest(Collections.singleton(playerView)));
        assertFalse(result.success());
    }

    @Test
    public void testJoin() {
        Instance instance = mock(Instance.class);
        InstanceConfig instanceConfig = new InstanceConfig(InstanceConfig.DEFAULT_POS);
        SceneFallback sceneFallback = (ignored) -> true;
        Lobby lobby = new Lobby(instance, instanceConfig, sceneFallback);
        Player player = mock(Player.class);
        PlayerView playerView = new PlayerView() {
            @Override
            public @NotNull UUID getUUID() {
                return playerUUID;
            }

            @Override
            public @NotNull CompletableFuture<String> getUsername() {
                return CompletableFuture.completedFuture(playerUUID.toString());
            }

            @Override
            public @NotNull Optional<Player> getPlayer() {
                return Optional.of(player);
            }

            @Override
            public @NotNull Optional<PlayerInfo> getPlayerInfo() {
                return Optional.of(new PlayerInfo(player, MinecraftServer.PROTOCOL_VERSION));
            }

            @Override
            public @NotNull CompletableFuture<Component> getDisplayName() {
                return CompletableFuture.completedFuture(Component.empty());
            }
        };

        RouteResult result = lobby.join(new BasicLobbyJoinRequest(Collections.singleton(playerView)));

        assertTrue(result.success());
        verify(player).setInstance(eq(instance), eq(instanceConfig.spawnPoint()));
    }

}
