package com.github.phantazmnetwork.api.game.scene.router;

import com.github.phantazmnetwork.api.game.scene.lobby.LobbyRouter;

/**
 * A static class container for reusable {@link SceneRouterKey}s.
 */
public class SceneRouterKeys {

    /**
     * Lobbies
     */
    public static final SceneRouterKey<LobbyRouter> LOBBY_DISPATCHER = new SceneRouterKey<>("lobby_dispatcher");

    private SceneRouterKeys() {

    }

}
