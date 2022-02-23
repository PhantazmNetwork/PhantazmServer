package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.SceneFallback;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class LobbyTest {

    private static final UUID playerUUID = UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127");

    @Test
    public void testShutdown() {
        Instance instance = Mockito.mock(Instance.class);
        InstanceConfig instanceConfig = new InstanceConfig(InstanceConfig.DEFAULT_POS);
        SceneFallback sceneFallback = (ignored) -> {};
        Lobby lobby = new Lobby(instance, instanceConfig, sceneFallback);
        PlayerView playerView = Mockito.mock(PlayerView.class);

        lobby.forceShutdown();
        Assertions.assertTrue(lobby.isShutdown());

        RouteResult result = lobby.join(new LobbyJoinRequest(Collections.singletonList(playerView)));
        Assertions.assertFalse(result.success());
    }

    @Test
    public void testJoin() {
        Instance instance = Mockito.mock(Instance.class);
        InstanceConfig instanceConfig = new InstanceConfig(InstanceConfig.DEFAULT_POS);
        SceneFallback sceneFallback = (ignored) -> {};
        Lobby lobby = new Lobby(instance, instanceConfig, sceneFallback);
        Player player = Mockito.mock(Player.class);
        PlayerView playerView = new PlayerView() {
            @Override
            public @NotNull UUID getUUID() {
                return playerUUID;
            }

            @Override
            public @NotNull Optional<Player> getPlayer() {
                return Optional.of(player);
            }
        };

        RouteResult result = lobby.join(new LobbyJoinRequest(Collections.singletonList(playerView)));

        Assertions.assertTrue(result.success());
        Mockito.verify(player).setInstance(ArgumentMatchers.eq(instance),
                ArgumentMatchers.eq(instanceConfig.spawnPoint()));
    }

}
