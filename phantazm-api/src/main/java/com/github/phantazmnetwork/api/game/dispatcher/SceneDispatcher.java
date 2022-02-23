package com.github.phantazmnetwork.api.game.dispatcher;

import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;

/**
 * Dispatches requests to scenes.
 * @param <TRequest> The type of request used for the dispatch.
 */
public interface SceneDispatcher<TRequest> extends Tickable {

    /**
     * Dispatches a request.
     * @param dispatchRequest The dispatch to request
     * @return The result of the dispatch
     */
    @NotNull DispatchResult dispatch(@NotNull TRequest dispatchRequest);

    /**
     * Sets whether the dispatcher should be considered joinable.
     * Implementations may define what joinable means as they like.
     * @param joinable Whether the dispatcher should be joinable
     */
    void setJoinable(boolean joinable);

    /**
     * Shuts down the dispatcher by force.
     */
    void forceShutdown();

}
