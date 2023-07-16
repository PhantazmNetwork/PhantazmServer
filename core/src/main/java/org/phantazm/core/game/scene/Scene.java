package org.phantazm.core.game.scene;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.Tickable;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerView;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a scene which accepts join requests.
 *
 * @param <TRequest> The type of request used for joins.
 */
public interface Scene<TRequest extends SceneJoinRequest> extends Tickable {

    /**
     * Gets the {@link PlayerView}s that are associated with the scene.
     * Some players might not be in game.
     *
     * @return A view of the {@link PlayerView}s associated the scene.
     */
    @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers();

    @NotNull UUID getUUID();

    @NotNull TransferResult join(@NotNull TRequest joinRequest);

    @NotNull TransferResult leave(@NotNull Iterable<UUID> leavers);

    /**
     * Gets the number of players that are considered "ingame" in the scene.
     * This count may differ from the number of entries in
     *
     * @return The number of players ingame
     */
    int getIngamePlayerCount();

    /**
     * Gets a reported value as a weight for join requests.
     * Greater values indicate that this scene should be preferred for joins.
     *
     * @param request The join request used to determine the scene's weight
     * @return The reported weight value
     */
    int getJoinWeight(@NotNull TRequest request);

    /**
     * Whether the scene is currently shutdown.
     * Shutdown scenes should not allow more players.
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
     * Sets whether the scene should be considered joinable.
     * Joinable is defined on an implementation basis.
     *
     * @param joinable Whether the scene should be joinable
     */
    void setJoinable(boolean joinable);

    @NotNull SceneFallback getFallback();

    boolean isQuittable();

    boolean acceptGhost(@NotNull PlayerView playerView);

    boolean hasGhost(@NotNull Player player);
}
