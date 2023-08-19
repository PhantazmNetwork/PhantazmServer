package org.phantazm.core.game.scene.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.npc.NPCHandler;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@EnvTest
public class LobbyIntegrationTest {

    private static final UUID playerUUID = UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127");

    @Test
    public void testShutdown(Env env) {
        Instance instance = env.createFlatInstance();
        InstanceConfig instanceConfig = new InstanceConfig(InstanceConfig.DEFAULT_POS, InstanceConfig.DEFAULT_TIME,
            InstanceConfig.DEFAULT_TIME_RATE, InstanceConfig.DEFAULT_CHUNK_LOAD_RANGE);
        SceneFallback sceneFallback = (ignored) -> CompletableFuture.completedFuture(true);
        Lobby lobby = new Lobby(UUID.randomUUID(), instance, instanceConfig, sceneFallback,
            new NPCHandler(List.of(), instance, instance.eventNode()), Collections.emptyList(),
            MiniMessage.miniMessage(), "", true, Mockito.mock(PlayerViewProvider.class),
            player -> CompletableFuture.completedFuture(Component.empty()));
        PlayerView playerView = mock(PlayerView.class);

        lobby.shutdown();
        assertTrue(lobby.isShutdown());

        TransferResult result = lobby.join(new BasicLobbyJoinRequest(Collections.singleton(playerView)));
        assertFalse(result.executor().isPresent());
    }

    /* TODO: fix
    @Test
    public void testJoin(Env env) {
        Instance instance = env.createFlatInstance();
        InstanceConfig instanceConfig = new InstanceConfig(InstanceConfig.DEFAULT_POS, InstanceConfig.DEFAULT_TIME,
                InstanceConfig.DEFAULT_TIME_RATE, InstanceConfig.DEFAULT_CHUNK_LOAD_RANGE);
        SceneFallback sceneFallback = (ignored) -> true;
        Lobby lobby = new Lobby(UUID.randomUUID(), instance, instanceConfig, sceneFallback,
                new NPCHandler(List.of(), instance));
        Player player = env.createPlayer(instance, instanceConfig.spawnPoint());
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
            public @NotNull Optional<String> getUsernameIfCached() {
                return Optional.of(playerUUID.toString());
            }

            @Override
            public @NotNull CompletableFuture<Component> getDisplayName() {
                return CompletableFuture.completedFuture(Component.empty());
            }

            @Override
            public @NotNull Optional<? extends Component> getDisplayNameIfCached() {
                return Optional.of(Component.empty());
            }

            @Override
            public @NotNull Optional<Player> getPlayer() {
                return Optional.of(player);
            }

        };

        TransferResult result =
                lobby.join(new BasicLobbyJoinRequest(env.process().connection(), Collections.singleton(playerView)));

        assertTrue(result.success());
        assertEquals(instance, player.getInstance());
        assertEquals(instanceConfig.spawnPoint(), player.getPosition());
    }
    */

}
