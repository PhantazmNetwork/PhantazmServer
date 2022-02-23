package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a scene which accepts join requests.
 * @param <TRequest> The type of request used for joins.
 */
public interface Scene<TRequest> extends Tickable {

    /**
     * Routes a join request to the scene.
     * @param joinRequest The request for the join
     * @return The result of the join
     */
    @NotNull RouteResult join(@NotNull TRequest joinRequest);

    @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers);

    /**
     * Gets the {@link PlayerView}s that are currently part of the scene.
     * @return
     */
    @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers();

    int getIngamePlayerCount();

    boolean isShutdown();

    void forceShutdown();

}
