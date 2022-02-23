package com.github.phantazmnetwork.api.game.scene.router;

import com.github.phantazmnetwork.api.game.scene.Scene;

/**
 * Routes requests to scenes.
 * @param <TRequest> The type of request used for the router.
 */
public interface SceneRouter<TRequest> extends Scene<TRequest> {

    /**
     * Sets whether the router should be considered joinable.
     * Joinable is defined on an implementation basis.
     * @param joinable Whether the router should be joinable
     */
    void setJoinable(boolean joinable);

    /**
     * Shuts down the router by force.
     */
    void forceShutdown();

}
