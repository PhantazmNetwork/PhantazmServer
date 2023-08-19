package org.phantazm.core.game.scene;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SceneRouter<TScene extends Scene<?>, TRequest extends SceneJoinRequest> extends Tickable, Keyed {

    @NotNull CompletableFuture<RouteResult> findScene(@NotNull TRequest joinRequest);

    @NotNull Collection<TScene> getScenes();

    @NotNull Optional<TScene> getCurrentScene(@NotNull UUID playerUUID);

    @NotNull Collection<TScene> getScenesContainingPlayer(@NotNull UUID playerUUID);

    /**
     * Whether the scene is currently shutdown. Shutdown scenes should not allow more players.
     *
     * @return Whether the scene is shutdown
     */
    boolean isShutdown();

    /**
     * Shuts down the scene and breaks its current lifecycle.
     */
    void shutdown();

    /**
     * Checks whether the scene is considered joinable.
     *
     * @return Whether the scene is considered joinable
     */
    boolean isJoinable();

    /**
     * Sets whether the scene should be considered joinable. Joinable is defined on an implementation basis.
     *
     * @param joinable Whether the scene should be joinable
     */
    void setJoinable(boolean joinable);

    /**
     * Whether this router is a "game".
     *
     * @return true if this router represents a game, false otherwise
     */
    boolean isGame();

    /**
     * Whether this router currently has any scenes with players in them.
     *
     * @return true if this router has any scenes containing players, false otherwise
     */
    boolean hasActiveScenes();

}
