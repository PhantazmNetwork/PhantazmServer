package org.phantazm.server;

import org.phantazm.core.game.scene.RouterKey;
import org.phantazm.core.game.scene.lobby.Lobby;
import org.phantazm.core.game.scene.lobby.LobbyRouteRequest;
import org.phantazm.core.game.scene.lobby.LobbyRouter;
import org.phantazm.zombies.scene.ZombiesRouteRequest;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

public final class RouterKeys {
    private RouterKeys() {
        throw new UnsupportedOperationException();
    }

    public static final RouterKey<ZombiesScene, ZombiesRouteRequest, ZombiesSceneRouter> ZOMBIES_SCENE_ROUTER =
            new RouterKey<>(ZombiesSceneRouter.class);

    public static final RouterKey<Lobby, LobbyRouteRequest, LobbyRouter> LOBBY_SCENE_ROUTER =
            new RouterKey<>(LobbyRouter.class);
}
