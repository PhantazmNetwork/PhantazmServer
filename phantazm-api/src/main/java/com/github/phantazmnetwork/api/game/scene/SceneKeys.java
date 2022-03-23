package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.game.scene.lobby.LobbyRouter;

/**
 * A static class container for reusable {@link SceneKey}s.
 */
public final class SceneKeys {

    /**
     * Lobbies
     */
    public static final SceneKey<LobbyRouter> LOBBY_ROUTER = new SceneKey<>("lobby_dispatcher");

    private SceneKeys() {
        throw new UnsupportedOperationException();
    }

}
