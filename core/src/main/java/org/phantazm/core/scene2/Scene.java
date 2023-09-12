package org.phantazm.core.scene2;

import net.minestom.server.Tickable;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.function.Function;

/**
 * Represents something that can be joined by players, such as a game or a lobby. Scenes are ticked by a
 * {@link SceneManager}.
 * <p>
 * Scene implementations are expected to have an identity-based {@link Object#equals(Object)} method (and, by extension,
 * {@link Object#hashCode()}). That is, their {@code equals} method should return true iff {@code this == obj} where
 * {@code this} is this object and {@code obj} is the object to which this one is being compared.
 * <p>
 * <h2>Thread Safety</h2>
 * Unless otherwise specified, all methods on Scene are <i>not</i> thread safe; the scene must be acquired using the
 * {@link Acquirable} API before modifications are made.
 */
public interface Scene extends Tickable, Acquirable.Source<Scene>, PacketGroupingAudience {
    /**
     * Gets a view of all players currently in the scene. This collection cannot be modified by callers, but can itself
     * change as players are added to or removed from the scene.
     *
     * @return a read-only view of the players currently in this scene
     */
    @NotNull @UnmodifiableView Set<@NotNull PlayerView> playersView();

    /**
     * Determines if this Scene contains the given player or not.
     *
     * @param playerView the {@link PlayerView} to check
     * @return {@code true} if this scene contains the player; {@code false} otherwise
     */
    default boolean hasPlayer(@NotNull PlayerView playerView) {
        return playersView().contains(playerView);
    }

    /**
     * Determines if a player exists in this scene such that {@link PlayerView#getUUID()} equals the given UUID.
     *
     * @param uuid the UUID to check for
     * @return {@code true} if this scene contains a player with the UUID; {@code false} otherwise
     */
    default boolean hasPlayer(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);
        return playersView().contains(PlayerView.lookup(uuid));
    }

    /**
     * Determines if a player exists in this scene.
     *
     * @param player the player to check for
     * @return {@code true} if this scene contains the player; {@code false} otherwise
     */
    default boolean hasPlayer(@NotNull Player player) {
        Objects.requireNonNull(player);
        return hasPlayer(player.getUuid());
    }

    /**
     * Gets the number of players currently in the scene.
     *
     * @return the number of players in the scene
     */
    int playerCount();

    /**
     * Returns whether this scene is joinable under any circumstances. When determining where to send player(s), this
     * can be queried to filter out the most obviously impossible candidates before performing more detailed checks.
     * <p>
     * This method must also return false if:
     * <ul>
     *     <li>The scene has been shut down by a previous call to {@link Scene#preShutdown()} (i.e. when {@link Scene#isShutdown()} returns {@code true})</li>
     * </ul>
     *
     * @return true if this scene can be joined, false otherwise
     */
    boolean joinable();

    /**
     * Whether this scene should prevent an orderly server shutdown. This should be {@code true} for ongoing games with
     * players that have not finished yet, but {@code false} for finished games, empty games, or lobbies.
     *
     * @return true if this scene prevents shutdown, false otherwise
     */
    boolean preventsServerShutdown();

    /**
     * Checks if the scene has been previously shut down via a call to {@link Scene#preShutdown()}.
     *
     * @return {@code true} if the scene has been shut down prior, {@code false} otherwise
     */
    boolean isShutdown();

    /**
     * Initializes the shutdown procedure for this scene. It is expected that, some point after calling this method, the
     * {@link SceneManager} will call {@link Scene#shutdown()} in order to conclude shutdown.
     * <p>
     * Calling this method will generally have the following effects:
     * <ul>
     *     <li>The scene will no longer accept new players; {@link Scene#joinable()} will return {@code false}.</li>
     *     <li>Subsequent calls to {@link Scene#isShutdown()} will return {@code true}.</li>
     * </ul>
     * <p>
     * This method should <i>not</i> perform clean up actions; rather, it should update state as necessary to ensure
     * this scene cannot be joined.
     * <p>
     * This method is marked as internal because, along with {@code shutdown}, it should only be called indirectly
     * through {@link SceneManager#removeScene(Scene, Function)}.
     */
    @ApiStatus.Internal
    void preShutdown();

    /**
     * Shuts down this scene, cleaning up any resources it might have been using. These include instances, event hooks,
     * and scheduled tasks. This method should only be called after calling {@link Scene#preShutdown()}.
     * Implementations
     * <i>may</i> throw an exception if {@code preShutdown} is not called first.
     * <p>
     * Players that exist in the scene when it is shut down <i>may</i> be forcefully kicked from the server. The
     * {@link SceneManager} instance should make a best-effort attempt to find somewhere to send old players before
     * calling this method.
     * <p>
     * This method is marked as internal because it should only be called indirectly through
     * {@link SceneManager#removeScene(Scene, Function)}, in order to prevent shut-down scenes from sticking around in
     * the manager, ensure players are properly removed, and prevent synchronization issues.
     */
    @ApiStatus.Internal
    void shutdown();

    /**
     * Removes some number of players from this scene, updating internal state as needed to account for this.
     * <p>
     * This method <i>must</i>:
     * <ul>
     *     <li>Ignore players that have never been added to the scene</li>
     *     <li>Ignore players that are in a different scene</li>
     *     <li>Modify the state of players that <i>are</i> present in this scene such as to remove any and all modifications applied by this scene</li>
     * </ul>
     * <p>
     * This method must <i>not</i>:
     * <ul>
     *     <li>Send the players to a new scene</li>
     *     <li>Attempt to remove players from other scenes</li>
     *     <li>Kick the players</li>
     *     <li>Modify player state for players not in the scene</li>
     * </ul>
     * <p>
     * This method is marked as internal because it should generally only be called by {@link SceneManager}; this method
     * is only used if it can be determined that a player will be accepted by a different scene, or if they are
     * disconnecting from the server (in which case there is no other scene).
     *
     * @param players the players who are leaving, some of which may not be online, or even present in the scene
     * @return a modifiable set of players that were actually removed from the scene
     */
    @ApiStatus.Internal
    @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players);

    /**
     * Called by the {@link SceneManager} after players that were previously in this scene join a new scene. Since the
     * players are no longer managed by this scene, this method should not touch the state of the players in
     * {@code leftPlayers}. This is primarily useful for sending tablist removal packets to players that <i>are</i>
     * currently in the scene.
     * <p>
     * {@code leftPlayers} is guaranteed to consist entirely of players that were previously returned by a call to
     * {@link Scene#leave(Set)}. That is, the set is guaranteed to contain only players that were successfully removed
     * from the scene with a {@code leave} call. However, some players <i>may</i> be offline. There will always be
     * exactly one call made to this method for every call to {@code leave}, unless the returned set is empty, in which
     * case the call to this method may be omitted.
     * <p>
     * As with similar methods on this interface, this is marked as internal because it should only be called by the
     * SceneManager, which does so directly after the players have joined a new scene, or alternatively after they are
     * removed for disconnecting.
     *
     * @param leftPlayers the players that previously left this scene
     */
    @ApiStatus.Internal
    void postLeave(@NotNull Set<@NotNull PlayerView> leftPlayers);

    @Override
    default @NotNull Collection<@NotNull Player> getPlayers() {
        Set<PlayerView> playerViews = playersView();
        List<Player> onlinePlayers = new ArrayList<>(playerViews.size());

        for (PlayerView view : playerViews) {
            view.getPlayer().ifPresent(onlinePlayers::add);
        }

        return onlinePlayers;
    }
}