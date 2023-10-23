package org.phantazm.core.scene2;

import org.phantazm.core.scene2.lobby.Lobby;

/**
 * Global {@link SceneManager.Key} instances.
 */
public final class CoreJoinKeys {
    private CoreJoinKeys() {
        throw new UnsupportedOperationException();
    }

    /**
     * The {@link SceneManager.Key} for joining the main lobby.
     */
    public static SceneManager.Key<Lobby> MAIN_LOBBY = SceneManager.joinKey(Lobby.class, "main");
}
