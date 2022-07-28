package com.github.phantazmnetwork.core.game.scene.lobby;

import com.github.phantazmnetwork.core.game.scene.Scene;
import com.github.phantazmnetwork.core.game.scene.SceneProvider;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

public class LobbyRouterTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRejectionWhenNotJoinable() {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        Scene<LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        router.setJoinable(false);
        LobbyRouteRequest request =
                new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(Collections.emptyList()));

        assertFalse(router.join(request).success());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRejectionWhenShutdown() {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        Scene<LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        router.forceShutdown();
        LobbyRouteRequest request =
                new LobbyRouteRequest(lobbyName, new BasicLobbyJoinRequest(Collections.emptyList()));

        assertFalse(router.join(request).success());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinRequestWithNoMatchingSceneProvider() {
        String lobbyName = "main";
        Map<String, SceneProvider<Lobby, LobbyJoinRequest>> sceneProviders =
                Collections.singletonMap(lobbyName, (SceneProvider<Lobby, LobbyJoinRequest>)mock(SceneProvider.class));
        Scene<LobbyRouteRequest> router = new LobbyRouter(sceneProviders);

        LobbyRouteRequest request =
                new LobbyRouteRequest("notMain", new BasicLobbyJoinRequest(Collections.emptyList()));

        assertFalse(router.join(request).success());
    }

}
