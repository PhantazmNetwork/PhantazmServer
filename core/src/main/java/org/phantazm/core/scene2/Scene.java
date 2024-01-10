package org.phantazm.core.scene2;

import net.minestom.server.Tickable;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.function.Function;

/**
 * Represents something that can be joined by players, such as a game or a lobby. Players can be sent between scenes,
 * but they can only be considered a "part of" one scene at a time.
 * <p>
 * Scene implementations are expected to have an identity-based {@link Object#equals(Object)} method (and, by extension,
 * {@link Object#hashCode()}). That is, their {@code equals} method should return true iff {@code this == obj} where
 * {@code this} is this object and {@code obj} is the object to which this one is being compared.
 * <p>
 * Unless specified by the method contract, Scene methods should <i>not</i> throw exceptions to avoid corrupting state.
 * Exceptions may or may not be handled gracefully by the SceneManager.
 * <p>
 * Players can <i>join</i> scenes using various methods on {@link SceneManager}. SceneManager also manages scene
 * lifetime: creation, ticking, and removal.
 * <p>
 * Methods on this interface that are annotated with {@link ApiStatus.Internal} should not be called outside of
 * SceneManager for any reason.
 * <p>
 * <h2>Thread Safety</h2>
 * Unless otherwise specified, all methods on Scene are <i>not</i> thread safe; the scene must be acquired using the
 * {@link Acquirable} API before modifications are made. However, all non-internal methods are safe to access without
 * synchronizing from the current <i>tick thread</i>. In other words, if calling from the {@link Scene#tick(long)}
 * method, it is not necessary to acquire before modifying scene state.
 *
 * @see SceneManager
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
        Objects.requireNonNull(playerView);
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
    default int playerCount() {
        return playersView().size();
    }

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
     * If this scene represents a "game" of some kind. Generally speaking, scenes that are games:
     *
     * <ul>
     *     <li>Will prevent the server from shutting down during an orderly shutdown, if the game is in-progress</li>
     *     <li>Can be "quit" using a command like {@code /quit} or similar</li>
     * </ul>
     * <p>
     * Defaults to {@code true}. Must remain constant for the scene's usable lifetime, and by extension it is safe to
     * call this method without acquiring the scene first.
     *
     * @return {@code true} if quittable, {@code false} otherwise
     */
    default boolean isGame() {
        return true;
    }

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
     * In other words, this method should update state as necessary to ensure this scene cannot be joined. Additionally,
     * implementations may find it useful to perform certain cleanup actions here, as players will <i>not</i> be
     * removed yet.
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
     *     <li>Correctly remove players who are offline but considered part of the scene still</li>
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
     * This method may be called for players that are disconnecting from the server. Therefore, their
     * {@link PlayerView#getPlayer()} method will return an empty Optional, as they are not connected to the server. If
     * they were previously present in this scene, this method must correctly update internal state to account for this,
     * and add the appropriate PlayerView to the set returned by this method.
     * <p>
     * This method is marked as internal because it should generally only be called by {@link SceneManager}; this method
     * is only used if it can be determined that a player will be accepted by a different scene, or if they are
     * disconnecting from the server (in which case there is no other scene).
     *
     * @param players the players who are leaving, some of which may not be online, or even present in the scene
     * @return a modifiable set of players that were actually removed from the scene, some of which may not be online
     */
    @ApiStatus.Internal
    @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players);

    /**
     * Called by the {@link SceneManager} after players that were previously in this scene either leave the server or
     * join a new scene. Since the players are no longer managed by this scene, this method should not touch the state
     * of the players in {@code leftPlayers}. This is primarily useful for sending tablist removal packets to players
     * that <i>are</i> currently in the scene.
     * <p>
     * {@code leftPlayers} is guaranteed to consist entirely of players that were previously returned by a call to
     * {@link Scene#leave(Set)}. That is, the set is guaranteed to contain only players that were successfully removed
     * from the scene with a {@code leave} call. There will always be exactly one call made to this method for every
     * call to {@code leave}, unless the set returned by such a call is empty, in which case the call to this method may
     * or may not be omitted.
     * <p>
     * Some of the players in the {@code leftPlayers} set may be offline, such as if they disconnected from the server
     * while in this scene.
     * <p>
     * As with similar methods on this interface, this is marked as internal because it should only be called by the
     * SceneManager, which does so directly after the players have joined a new scene, or alternatively after they are
     * removed for disconnecting.
     *
     * @param leftPlayers the players that previously left this scene
     */
    @ApiStatus.Internal
    void postLeave(@NotNull Set<? extends @NotNull Player> leftPlayers);

    @Override
    default @NotNull Collection<@NotNull Player> getPlayers() {
        return PlayerView.getMany(playersView(), ArrayList::new);
    }

    default @NotNull Optional<SceneManager.Key<?>> getDefaultJoinKey() {
        return Optional.empty();
    }

    /**
     * Gets a {@link TagHandler} for a certain player present in this scene. This should not be cleared until the scene
     * is shut down.
     * <p>
     * This method must be thread-safe.
     *
     * @param player the UUID of the player
     * @return the {@link TagHandler} for this player
     */
    @NotNull TagHandler playerTags(@NotNull UUID player);

    /**
     * Works equivalently to {@link Scene#playerTags(UUID)}, but accepts a player rather than a {@link UUID}. The
     * default implementation delegates to {@link Scene#playerTags(UUID)}.
     *
     * @param player the player to retrieve tags for
     * @return the {@link TagHandler} for this player
     */
    default @NotNull TagHandler playerTags(@NotNull Player player) {
        return playerTags(player.getUuid());
    }

    /**
     * Works equivalently to {@link Scene#playerTags(UUID)}, but accepts a {@link PlayerView} rather than a
     * {@link UUID}. The default implementation delegates to {@link Scene#playerTags(UUID)}.
     *
     * @param playerView the player to retrieve tags for
     * @return the {@link TagHandler} for this player
     */
    default @NotNull TagHandler playerTags(@NotNull PlayerView playerView) {
        return playerTags(playerView.getUUID());
    }
}
