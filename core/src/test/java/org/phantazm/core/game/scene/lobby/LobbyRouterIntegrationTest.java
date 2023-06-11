package org.phantazm.core.game.scene.lobby;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

@EnvTest
public class LobbyRouterIntegrationTest {

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    @Test
    public void testJoinRejectionWhenNotJoinable(Env env) {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        Scene<LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        router.setJoinable(false);
        LobbyRouteRequest request = new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(env.process()
                .connection(), List.of()));

        assertFalse(router.join(request).success());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRejectionWhenShutdown(Env env) {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        Scene<LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        router.forceShutdown();
        LobbyRouteRequest request = new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(env.process()
                .connection(), List.of()));

        assertFalse(router.join(request).success());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRequestWithNoMatchingSceneProvider(Env env) {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        Scene<LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        LobbyRouteRequest request = new LobbyRouteRequest("notMain", new BasicLobbyJoinRequest(env.process()
                .connection(), List.of()));

        assertFalse(router.join(request).success());
    }

}
