package org.phantazm.core.game.scene.lobby;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneRouter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@EnvTest
public class LobbyRouterIntegrationTest {

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    @Test
    public void testJoinRejectionWhenNotJoinable(Env env) {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        SceneRouter<Lobby, LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        router.setJoinable(false);
        LobbyRouteRequest request = new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(List.of()));

        assertTrue(router.findScene(request).join().scene().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRejectionWhenShutdown(Env env) {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        SceneRouter<Lobby, LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        router.shutdown();
        LobbyRouteRequest request = new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(List.of()));

        assertTrue(router.findScene(request).join().scene().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRequestWithNoMatchingSceneProvider(Env env) {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        SceneRouter<Lobby, LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        LobbyRouteRequest request = new LobbyRouteRequest("notMain", new BasicLobbyJoinRequest(List.of()));

        assertTrue(router.findScene(request).join().scene().isEmpty());
    }

}
