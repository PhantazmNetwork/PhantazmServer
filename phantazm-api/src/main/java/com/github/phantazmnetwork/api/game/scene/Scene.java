package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Represents a scene which accepts join requests.
 * @param <TRequest> The type of request used for joins.
 */
public interface Scene<TRequest> extends Tickable {

    /**
     * Joins the scene.
     * @param joinRequest The request for the join
     * @return The result of the join
     */
    @NotNull JoinResult join(@NotNull TRequest joinRequest);

    /**
     * Gets the {@link PlayerView}s that are currently part of the scene.
     * @return
     */
    @UnmodifiableView @NotNull Iterable<PlayerView> getPlayers();

    int getOnlinePlayerCount();

    boolean isShutdown();

    void forceShutdown();

}
