package org.phantazm.server;

import org.phantazm.core.game.scene.RouterKey;
import org.phantazm.zombies.scene.ZombiesRouteRequest;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

public final class RouterKeys {
    public static final RouterKey<ZombiesScene, ZombiesRouteRequest, ZombiesSceneRouter> ZOMBIES_SCENE_ROUTER =
        new RouterKey<>(ZombiesSceneRouter.class);

    private RouterKeys() {
        throw new UnsupportedOperationException();
    }
}
